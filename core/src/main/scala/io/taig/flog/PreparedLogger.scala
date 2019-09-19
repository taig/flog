package io.taig.flog

import java.time.Instant

import cats.Applicative
import io.circe.JsonObject

final class PreparedLogger[F[_]](
    prepare: Event => Event,
    logger: Logger[F]
) extends Logger[F] {
  override def apply(event: Instant => Event): F[Unit] =
    logger { timestamp =>
      prepare(event(timestamp))
    }
}

object PreparedLogger {
  def apply[F[_]: Applicative](
      prepare: Event => Event,
      logger: Logger[F]
  ): Logger[F] = new PreparedLogger[F](prepare, logger)

  def prefixed[F[_]: Applicative](prefix: Scope, logger: Logger[F]): Logger[F] =
    PreparedLogger[F](
      event => event.copy(scope = prefix ++ event.scope),
      logger
    )

  def scoped[F[_]: Applicative](scope: Scope, logger: Logger[F]): Logger[F] =
    PreparedLogger[F](_.copy(scope = scope), logger)

  def payload[F[_]: Applicative](
      payload: JsonObject,
      logger: Logger[F]
  ): Logger[F] = {
    val prepare = { event: Event =>
      val update = event.payload.map { json =>
        JsonObject.fromMap(payload.toMap ++ json.toMap)
      }

      event.copy(payload = update)
    }

    PreparedLogger[F](prepare, logger)
  }
}