package io.taig.logging

import cats._
import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.taig.logging.internal.Helpers

trait Logger[F[_]] {
  def apply(events: List[Event]): F[Unit]

  final def apply(
      level: Level,
      scope: Scope,
      message: Eval[String] = Eval.now(""),
      payload: Eval[Json] = Eval.now(Json.Null),
      throwable: Option[Throwable] = None
  )(implicit F: Sync[F]): F[Unit] =
    Helpers.timestamp[F].map { timestamp =>
      List(Event(level, scope, timestamp, message, payload, throwable))
    } >>= apply

  final def debug(
      scope: Scope,
      message: => String = "",
      payload: => Json = Json.Null
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Debug, scope, Eval.later(message), Eval.later(payload), None)

  final def error(
      scope: Scope,
      message: => String = "",
      payload: => Json = Json.Null
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Error, scope, Eval.later(message), Eval.later(payload), None)

  final def info(
      scope: Scope,
      message: => String = "",
      payload: => Json = Json.Null
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Info, scope, Eval.later(message), Eval.later(payload), None)

  final def failure(
      scope: Scope,
      message: => String = "",
      payload: => Json = Json.Null
  )(throwable: Throwable)(implicit F: Sync[F]): F[Unit] =
    apply(
      Level.Failure,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable.some
    )

  final def warning(
      scope: Scope,
      message: => String = "",
      payload: => Json = Json.Null
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Warning, scope, Eval.later(message), Eval.later(payload), None)
}
