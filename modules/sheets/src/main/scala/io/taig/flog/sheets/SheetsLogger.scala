package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.{Blocker, Clock, ContextShift, Sync}
import cats.implicits._
import io.circe.Json
import io.taig.flog.Logger
import io.taig.flog.data.Event
import io.taig.flog.sheets.util.Google
import io.taig.flog.util.{Circe, Printer}

object SheetsLogger {
  def apply[F[_]: Sync: ContextShift: Clock](
      blocker: Blocker,
      account: F[InputStream],
      id: String,
      range: String,
      schema: List[String]
  ): F[Logger[F]] =
    Google.sheets[F](blocker, account).map { sheets =>
      Logger { events =>
        val rows = events.map(row(schema, _))
        Google.append(sheets, id, range)(rows).void
      }
    }

  def row(schema: List[String], event: Event): List[AnyRef] = {
    val payload = Circe.flatten(Json.fromJsonObject(event.payload)).map {
      case (key, value) => key -> value.noSpaces
    }
    val known: List[String] = schema.map(payload.getOrElse(_, ""))
    val unknown: List[String] = schema.foldLeft(payload)(_ - _).values.toList

    List(
      Printer.timestamp(event.timestamp),
      event.level.show,
      event.scope.show,
      event.message,
      event.throwable.map(Printer.throwable).orEmpty
    ) ++ known ++ unknown
  }
}
