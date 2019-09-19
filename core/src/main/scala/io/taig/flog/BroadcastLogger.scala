package io.taig.flog

import cats.effect.Sync
import cats.implicits._

final class BroadcastLogger[F[_]: Sync](loggers: List[Logger[F]])
    extends SyncLogger[F] {
  override def apply(event: Event): F[Unit] =
    loggers.traverse_(_.apply(_ => event))
}

object BroadcastLogger {
  def apply[F[_]: Sync](loggers: List[Logger[F]]): Logger[F] =
    new BroadcastLogger[F](loggers)

  def of[F[_]: Sync](loggers: Logger[F]*): Logger[F] =
    BroadcastLogger(loggers.toList)
}
