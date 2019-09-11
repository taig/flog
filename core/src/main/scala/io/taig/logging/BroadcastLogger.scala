package io.taig.logging

import cats._
import cats.implicits._

final class BroadcastLogger[F[_]: Applicative](loggers: List[Logger[F]])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] =
    loggers.traverse_(_.apply(events))
}

object BroadcastLogger {
  def apply[F[_]: Applicative](loggers: List[Logger[F]]): Logger[F] =
    new BroadcastLogger[F](loggers)

  def of[F[_]: Applicative](loggers: Logger[F]*): Logger[F] =
    BroadcastLogger(loggers.toList)
}
