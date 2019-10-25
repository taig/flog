package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.taig.flog.internal.Shows._
import io.taig.flog.sheets.internal.{Circe, Google}
import io.taig.flog.{Event, Logger}

object SheetsLogger {
  def apply[F[_]: Sync](
      credentials: F[InputStream]
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    Google.sheets[F](credentials).map { sheets =>
      Logger { event =>
        Google.append(sheets, id, range)(List(row(schema, event))).void
      }
    }

  def fromResource[F[_]: Sync](
      resource: String
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    SheetsLogger(Google.resource[F](resource))(id, range, schema)

  def row(schema: List[String], event: Event): List[AnyRef] = {
    val payload = Circe.flatten(Json.fromJsonObject(event.payload.value))
    val known: List[String] = schema.map(payload.getOrElse(_, ""))
    val unknown: List[String] = schema.foldLeft(payload)(_ - _).values.toList

    List(
      event.timestamp.show,
      event.level.show,
      event.scope.show,
      event.message.value,
      event.throwable.map(_.show).orEmpty
    ) ++ known ++ unknown
  }
}
