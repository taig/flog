package io.taig.flog

import cats.Applicative

final class NoopLogger[F[_]](implicit F: Applicative[F])
    extends BroadcastLogger[F](List.empty) {
  override def apply(events: List[Event]): F[Unit] = F.unit
}

object NoopLogger {
  def apply[F[_]](implicit F: Applicative[F]): Logger[F] = new NoopLogger[F]
}
