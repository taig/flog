package io.taig.flog

import java.time.Instant

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.Time

abstract class SyncLogger[F[_]: Sync] extends Logger[F] {
  override final def apply(event: Instant => Event): F[Unit] =
    Time.now[F].flatMap(timestamp => apply(event(timestamp)))

  def apply(event: Event): F[Unit]
}
