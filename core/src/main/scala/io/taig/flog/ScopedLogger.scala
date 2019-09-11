package io.taig.flog

final class ScopedLogger[F[_]](scope: Scope, logger: Logger[F])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] =
    logger(events.map(event => event.copy(scope = scope ++ event.scope)))
}

object ScopedLogger {
  def apply[F[_]](scope: Scope, logger: Logger[F]): Logger[F] =
    new ScopedLogger[F](scope, logger)
}
