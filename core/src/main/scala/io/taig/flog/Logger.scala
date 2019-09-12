package io.taig.flog

import cats._
import cats.effect.Sync
import cats.implicits._
import io.taig.flog.Logger.EmptyPayload
import io.taig.flog.internal.Helpers

trait Logger[F[_]] {
  def apply(events: List[Event]): F[Unit]

  final def apply(
      level: Level,
      scope: Scope,
      message: Eval[String] = Eval.now(""),
      payload: Eval[Map[String, String]] = Eval.now(EmptyPayload),
      throwable: Option[Throwable] = None
  )(implicit F: Sync[F]): F[Unit] =
    Helpers.timestamp[F].map { timestamp =>
      List(Event(level, scope, timestamp, message, payload, throwable))
    } >>= apply

  final def debug(
      scope: Scope,
      message: => String,
      payload: => Map[String, String]
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Debug, scope, Eval.later(message), Eval.later(payload), None)

  final def debug(
      scope: Scope,
      message: => String,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] =
    debug(scope, message, payload.toMap)

  final def debug(
      scope: Scope,
      message: => String
  )(implicit F: Sync[F]): F[Unit] = debug(scope, message, EmptyPayload)

  final def debug(
      scope: Scope,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] = debug(scope, "", payload.toMap)

  final def error(
      scope: Scope,
      message: => String,
      payload: => Map[String, String]
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Error, scope, Eval.later(message), Eval.later(payload), None)

  final def error(
      scope: Scope,
      message: => String,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] =
    error(scope, message, payload.toMap)

  final def error(
      scope: Scope,
      message: => String
  )(implicit F: Sync[F]): F[Unit] =
    error(scope, message, EmptyPayload)

  final def error(
      scope: Scope,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] =
    error(scope, "", payload.toMap)

  final def info(
      scope: Scope,
      message: => String,
      payload: => Map[String, String]
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Info, scope, Eval.later(message), Eval.later(payload), None)

  final def info(
      scope: Scope,
      message: => String,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] = info(scope, message, payload.toMap)

  final def info(
      scope: Scope,
      message: => String
  )(implicit F: Sync[F]): F[Unit] = info(scope, message, EmptyPayload)

  final def info(
      scope: Scope,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] = info(scope, "", payload.toMap)

  final def failure(
      scope: Scope,
      message: => String,
      payload: => Map[String, String]
  )(throwable: Throwable)(implicit F: Sync[F]): F[Unit] =
    apply(
      Level.Failure,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable.some
    )

  final def failure(
      scope: Scope,
      message: => String,
      payload: (String, String)*
  )(throwable: Throwable)(implicit F: Sync[F]): F[Unit] =
    failure(scope, message, payload.toMap)(throwable)

  final def failure(
      scope: Scope,
      message: => String
  )(throwable: Throwable)(implicit F: Sync[F]): F[Unit] =
    failure(scope, message, EmptyPayload)(throwable)

  final def failure(
      scope: Scope,
      payload: (String, String)*
  )(throwable: Throwable)(implicit F: Sync[F]): F[Unit] =
    failure(scope, "", payload.toMap)(throwable)

  final def warning(
      scope: Scope,
      message: => String,
      payload: => Map[String, String]
  )(implicit F: Sync[F]): F[Unit] =
    apply(Level.Warning, scope, Eval.later(message), Eval.later(payload), None)

  final def warning(
      scope: Scope,
      message: => String,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] = warning(scope, message, payload.toMap)

  final def warning(
      scope: Scope,
      message: => String
  )(implicit F: Sync[F]): F[Unit] = warning(scope, message, EmptyPayload)

  final def warning(
      scope: Scope,
      payload: (String, String)*
  )(implicit F: Sync[F]): F[Unit] = warning(scope, "", payload.toMap)
}

object Logger {
  val EmptyPayload: Map[String, String] = Map.empty
}
