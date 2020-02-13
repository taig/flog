package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.{Clock, Sync}
import cats.implicits._
import io.circe.Json
import io.taig.flog.util.{Circe, Printer}
import io.taig.flog.algebra.Logger
import io.taig.flog.data.Event
import io.taig.flog.sheets.util.Google

object SheetsLogger {
  def apply[F[_]: Sync: Clock](
      credentials: F[InputStream]
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    Google.sheets[F](credentials).map { sheets =>
      Logger { event =>
        Google.append(sheets, id, range)(List(row(schema, event))).void
      }
    }

  def fromResource[F[_]: Sync: Clock](
      resource: String
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    SheetsLogger(Google.resource[F](resource))(id, range, schema)

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
