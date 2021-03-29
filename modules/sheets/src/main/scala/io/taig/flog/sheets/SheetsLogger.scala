package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.{Clock, Sync}
import cats.syntax.all._
import io.taig.flog.Logger
import io.taig.flog.data.Event
import io.taig.flog.sheets.util.Google
import io.taig.flog.util.{StacktracePrinter, TimestampPrinter}

object SheetsLogger {
  def apply[F[_]: Sync: ContextShift: Clock](account: F[InputStream],
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
    val payload = event.payload.flatten
    val known: List[String] = schema.map(payload.getOrElse(_, ""))
    val unknown: List[String] = schema.foldLeft(payload)(_ - _).values.toList

    List(
      TimestampPrinter(event.timestamp),
      event.level.show,
      event.scope.show,
      event.message,
      event.throwable.map(StacktracePrinter(_)).orEmpty
    ) ++ known ++ unknown
  }
}
