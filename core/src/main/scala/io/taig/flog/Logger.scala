package io.taig.flog

import java.time.Instant
import java.util.UUID

import cats._
import io.circe.{Json, JsonObject}
import io.circe.syntax._

abstract class Logger[F[_]](
    val prefix: Scope = Scope.Root,
    val preset: JsonObject = JsonObject.empty
) {
  def apply(event: Instant => Event): F[Unit]

  final def prefix(scope: Scope): Logger[F] =
    Logger(scope ++ prefix, preset, apply)

  final def add(payload: JsonObject): Logger[F] =
    Logger(prefix, JsonObject.fromMap(preset.toMap ++ payload.toMap), apply)

  final def add(fields: (String, Json)*): Logger[F] =
    add(JsonObject(fields: _*))

  final def trace(id: UUID): Logger[F] = add("trace" -> id.asJson)

  final def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: Eval[String] = Eval.now(""),
      payload: Eval[JsonObject] = Eval.now(JsonObject.empty),
      throwable: Option[Throwable] = None
  ): F[Unit] = apply { timestamp =>
    Event(
      level,
      prefix ++ scope,
      timestamp,
      message,
      payload.map(payload => JsonObject.fromMap(preset.toMap ++ payload.toMap)),
      throwable
    )
  }

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
}

object Logger {
  def apply[F[_]](
      prefix: Scope,
      payload: JsonObject,
      f: (Instant => Event) => F[Unit]
  ): Logger[F] = new Logger[F](prefix, payload) {
    override def apply(event: Instant => Event): F[Unit] = f(event)
  }

  def apply[F[_]](
      f: (Instant => Event) => F[Unit]
  ): Logger[F] = new Logger[F]() {
    override def apply(event: Instant => Event): F[Unit] = f(event)
  }
}
