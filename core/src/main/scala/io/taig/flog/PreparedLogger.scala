package io.taig.flog

import io.circe.JsonObject

final class PreparedLogger[F[_]](prepare: Event => Event, logger: Logger[F])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] =
    logger(events.map(prepare))
}

object PreparedLogger {
  def apply[F[_]](prepare: Event => Event, logger: Logger[F]): Logger[F] =
    new PreparedLogger[F](prepare, logger)

  def prefixed[F[_]](prefix: Scope, logger: Logger[F]): Logger[F] =
    PreparedLogger[F](
      event => event.copy(scope = prefix ++ event.scope),
      logger
    )

  def scoped[F[_]](scope: Scope, logger: Logger[F]): Logger[F] =
    PreparedLogger[F](_.copy(scope = scope), logger)

  def payload[F[_]](payload: JsonObject, logger: Logger[F]): Logger[F] = {
    val prepare = { event: Event =>
      val update = event.payload.map { json =>
        JsonObject.fromMap(payload.toMap ++ json.toMap)
      }

      event.copy(payload = update)
    }

    PreparedLogger[F](prepare, logger)
  }
}
