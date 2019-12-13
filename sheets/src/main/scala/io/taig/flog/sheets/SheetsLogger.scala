package io.taig.flog.sheets

import java.io.InputStream

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.taig.flog.internal.Shows._
import io.taig.flog.internal.Times
import io.taig.flog.sheets.internal.{Circe, Google}
import io.taig.flog.{Event, Logger}

object SheetsLogger {
  def apply[F[_]: Sync](
      credentials: F[InputStream]
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    Google.sheets[F](credentials).map { sheets =>
      Logger { event =>
        row(schema, event).flatMap { row =>
          Google.append(sheets, id, range)(List(row)).void
        }
      }
    }

  def fromResource[F[_]: Sync](
      resource: String
  )(id: String, range: String, schema: List[String]): F[Logger[F]] =
    SheetsLogger(Google.resource[F](resource))(id, range, schema)

  def row[F[_]: Sync](schema: List[String], event: Event): F[List[AnyRef]] = {
    val payload = Circe.flatten(Json.fromJsonObject(event.payload))
    val known: List[String] = schema.map(payload.getOrElse(_, ""))
    val unknown: List[String] = schema.foldLeft(payload)(_ - _).values.toList

    Times.now[F].map { timestamp =>
      List(
        timestamp.show,
        event.level.show,
        event.scope.show,
        event.message,
        event.throwable.map(_.show).orEmpty
      ) ++ known ++ unknown
    }

  }
}
