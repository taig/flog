package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.Sync
import cats.implicits._
import com.google.api.services.sheets.v4.Sheets
import io.taig.flog.internal.Helpers
import io.taig.flog.{Event, Logger}

final class SheetsLogger[F[_]: Sync](
    sheets: Sheets,
    id: String,
    range: String,
    schema: List[String]
) extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] =
    SheetsHelpers.append(sheets, id, range)(events.map(row)).void

  def row(event: Event): List[AnyRef] = {
    val payload = event.payload.value
    val known: List[String] = schema.map(payload.getOrElse(_, ""))
    val unknown: List[String] = schema.foldLeft(payload)(_ - _).values.toList

    List(
      Helpers.TimeFormatter.format(event.timestamp),
      event.level.show,
      event.scope.show,
      event.message.value,
      event.throwable.map(Helpers.print).orEmpty
    ) ++ known ++ unknown
  }
}

object SheetsLogger {
  def apply[F[_]: Sync](
      credentials: F[InputStream]
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    SheetsHelpers
      .sheets[F](credentials)
      .map(new SheetsLogger[F](_, id, range, schema))

  def fromResource[F[_]: Sync](
      resource: String
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    SheetsLogger(SheetsHelpers.resource[F](resource))(id, range, schema)
}
