package io.taig.flog

import cats.effect.Sync
import cats.implicits._

object BroadcastLogger {
  def apply[F[_]: Sync](loggers: List[Logger[F]]): Logger[F] =
    SyncLogger[F](event => loggers.traverse_(_.apply(_ => event)))

  def of[F[_]: Sync](loggers: Logger[F]*): Logger[F] =
    BroadcastLogger(loggers.toList)
}
