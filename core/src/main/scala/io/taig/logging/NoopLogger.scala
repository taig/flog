package io.taig.logging

import cats.Applicative

final class NoopLogger[F[_]](implicit F: Applicative[F]) extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] = F.unit
}

object NoopLogger {
  def apply[F[_]: Applicative]: Logger[F] = new NoopLogger[F]
}
