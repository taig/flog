package io.taig.flog

import cats.Eval
import cats.effect.Sync
import cats.implicits._
import io.circe.JsonObject
import io.taig.flog.internal.Time

abstract class SyncLogger[F[_]: Sync] extends Logger[F] {
  override final def apply(
      level: Level,
      scope: Scope,
      message: Eval[String],
      payload: Eval[JsonObject],
      throwable: Option[Throwable]
  ): F[Unit] =
    Time.now
      .map { timestamp =>
        List(Event(level, scope, timestamp, message, payload, throwable))
      }
      .flatMap(this.apply)
}
