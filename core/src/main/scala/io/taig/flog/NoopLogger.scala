package io.taig.flog

import java.time.Instant

import cats.Applicative

final class NoopLogger[F[_]](implicit F: Applicative[F]) extends Logger[F] {
  override def apply(events: Instant => List[Event]): F[Unit] = F.unit
}

object NoopLogger {
  def apply[F[_]](implicit F: Applicative[F]): Logger[F] = new NoopLogger[F]
}
