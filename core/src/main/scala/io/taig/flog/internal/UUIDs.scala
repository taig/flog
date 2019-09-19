package io.taig.flog.internal

import java.util.UUID

import cats.effect.Sync

private[flog] object UUIDs {
  def random[F[_]](implicit F: Sync[F]): F[UUID] = F.delay(UUID.randomUUID())
}
