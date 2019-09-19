package io.taig.flog

import java.util.UUID

import cats._
import io.circe.syntax._
import io.circe.{Json, JsonObject}

abstract class Logger[F[_]] {
  def apply(events: List[Event]): F[Unit]

  def apply(
      level: Level,
      scope: Scope,
      message: Eval[String] = Eval.now(""),
      payload: Eval[JsonObject] = Eval.now(JsonObject.empty),
      throwable: Option[Throwable] = None
  ): F[Unit]

  final def debug(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Debug,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def error(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Error,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def info(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Info,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def failure(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Failure,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def warning(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Warning,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def prefix(scope: Scope)(implicit F: Applicative[F]): Logger[F] =
    PreparedLogger.prefixed(scope, this)

  final def payload(value: JsonObject)(implicit F: Applicative[F]): Logger[F] =
    PreparedLogger.payload(value, this)

  final def payload(
      fields: (String, Json)*
  )(implicit F: Applicative[F]): Logger[F] =
    payload(JsonObject(fields: _*))

  final def tracer(trace: UUID)(implicit F: Applicative[F]): Logger[F] =
    payload("trace" -> trace.asJson)
}
