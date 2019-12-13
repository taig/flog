package io.taig.flog.internal

import java.time.Instant

import cats.effect.Sync

private[flog] object Times {
  def currentTimeMillis[F[_]](implicit F: Sync[F]): F[Long] =
    F.delay(System.currentTimeMillis())

  def now[F[_]](implicit F: Sync[F]): F[Instant] = F.delay(Instant.now())
}
