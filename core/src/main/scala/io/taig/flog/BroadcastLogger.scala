package io.taig.flog

import cats.implicits._
import cats.{Applicative, Eval}
import io.circe.JsonObject

abstract class BroadcastLogger[F[_]: Applicative](loggers: List[Logger[F]])
    extends Logger[F] {
  override final def apply(
      level: Level,
      scope: Scope,
      message: Eval[String],
      payload: Eval[JsonObject],
      throwable: Option[Throwable]
  ): F[Unit] =
    loggers.traverse_(_.apply(level, scope, message, payload, throwable))
}

object BroadcastLogger {
  def apply[F[_]: Applicative](loggers: List[Logger[F]]): Logger[F] =
    new BroadcastLogger[F](loggers) {
      override def apply(events: List[Event]): F[Unit] =
        loggers.traverse_(_.apply(events))
    }

  def of[F[_]: Applicative](loggers: Logger[F]*): Logger[F] =
    BroadcastLogger(loggers.toList)
}
