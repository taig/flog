package io.taig.flog

import io.taig.flog.data.{Context, Event, Level, Payload, Scope}
import cats.syntax.all._

trait LoggerLike[F[_[_]], G[_]] {
  def log(events: Long => List[Event]): G[Unit]

  def modify(f: List[Event] => List[Event]): F[G]

  final def filter(filter: Event => Boolean): F[G] = modify(_.filter(filter))

  final def minimum(level: Level): F[G] = filter(_.level >= level)

  final def defaults(context: Context): F[G] = modify(_.map(_.defaults(context)))

  /** Prefix all events of this logger with the given `Scope` */
  final def prefix(scope: Scope): F[G] = defaults(Context(scope, Payload.Empty))

  final def presets(payload: Payload.Object): F[G] = defaults(Context(Scope.Root, payload))

  final def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => Payload.Object = Payload.Empty,
      throwable: Option[Throwable] = None
  ): G[Unit] = log(timestamp => List(Event(timestamp, level, scope, message, payload, throwable)))

  final def debug(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): G[Unit] = apply(Level.Debug, scope, message, payload, throwable)

  final def debug(message: String): G[Unit] = debug(Scope.Root, message, Payload.Empty, None)
  final def debug(scope: Scope, message: String): G[Unit] = debug(scope, message, Payload.Empty, None)
  final def debug(payload: => Payload.Object): G[Unit] = debug(Scope.Root, "", payload, None)
  final def debug(scope: Scope, payload: => Payload.Object): G[Unit] = debug(scope, "", payload, None)
  final def debug(message: String, payload: => Payload.Object): G[Unit] = debug(Scope.Root, message, payload, None)
  final def debug(scope: Scope, message: String, payload: => Payload.Object): G[Unit] =
    debug(scope, message, payload, None)
  final def debug(message: String, throwable: Throwable): G[Unit] =
    debug(Scope.Root, message, Payload.Empty, Some(throwable))
  final def debug(scope: Scope, message: String, throwable: Throwable): G[Unit] =
    debug(scope, message, Payload.Empty, Some(throwable))
  final def debug(message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    debug(Scope.Root, message, payload, Some(throwable))
  final def debug(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    debug(scope, message, payload, Some(throwable))

  final def error(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): G[Unit] = apply(Level.Error, scope, message, payload, throwable)

  final def error(message: String): G[Unit] = error(Scope.Root, message, Payload.Empty, None)
  final def error(scope: Scope, message: String): G[Unit] = error(scope, message, Payload.Empty, None)
  final def error(payload: => Payload.Object): G[Unit] = error(Scope.Root, "", payload, None)
  final def error(scope: Scope, payload: => Payload.Object): G[Unit] = error(scope, "", payload, None)
  final def error(message: String, payload: => Payload.Object): G[Unit] = error(Scope.Root, message, payload, None)
  final def error(scope: Scope, message: String, payload: => Payload.Object): G[Unit] =
    error(scope, message, payload, None)
  final def error(message: String, throwable: Throwable): G[Unit] =
    error(Scope.Root, message, Payload.Empty, Some(throwable))
  final def error(scope: Scope, message: String, throwable: Throwable): G[Unit] =
    error(scope, message, Payload.Empty, Some(throwable))
  final def error(message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    error(Scope.Root, message, payload, Some(throwable))
  final def error(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    error(scope, message, payload, Some(throwable))

  final def info(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): G[Unit] = apply(Level.Info, scope, message, payload, throwable)

  final def info(message: String): G[Unit] = info(Scope.Root, message, Payload.Empty, None)
  final def info(scope: Scope, message: String): G[Unit] = info(scope, message, Payload.Empty, None)
  final def info(payload: => Payload.Object): G[Unit] = info(Scope.Root, "", payload, None)
  final def info(scope: Scope, payload: => Payload.Object): G[Unit] = info(scope, "", payload, None)
  final def info(message: String, payload: => Payload.Object): G[Unit] = info(Scope.Root, message, payload, None)
  final def info(scope: Scope, message: String, payload: => Payload.Object): G[Unit] =
    info(scope, message, payload, None)
  final def info(message: String, throwable: Throwable): G[Unit] =
    info(Scope.Root, message, Payload.Empty, Some(throwable))
  final def info(scope: Scope, message: String, throwable: Throwable): G[Unit] =
    info(scope, message, Payload.Empty, Some(throwable))
  final def info(message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    info(Scope.Root, message, payload, Some(throwable))
  final def info(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    info(scope, message, payload, Some(throwable))

  final def warning(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): G[Unit] = apply(Level.Warning, scope, message, payload, throwable)

  final def warning(message: String): G[Unit] = warning(Scope.Root, message, Payload.Empty, None)
  final def warning(scope: Scope, message: String): G[Unit] = warning(scope, message, Payload.Empty, None)
  final def warning(payload: => Payload.Object): G[Unit] = warning(Scope.Root, "", payload, None)
  final def warning(scope: Scope, payload: => Payload.Object): G[Unit] = warning(scope, "", payload, None)
  final def warning(message: String, payload: => Payload.Object): G[Unit] = warning(Scope.Root, message, payload, None)
  final def warning(scope: Scope, message: String, payload: => Payload.Object): G[Unit] =
    warning(scope, message, payload, None)
  final def warning(message: String, throwable: Throwable): G[Unit] =
    warning(Scope.Root, message, Payload.Empty, Some(throwable))
  final def warning(scope: Scope, message: String, throwable: Throwable): G[Unit] =
    warning(scope, message, Payload.Empty, Some(throwable))
  final def warning(message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    warning(Scope.Root, message, payload, Some(throwable))
  final def warning(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): G[Unit] =
    warning(scope, message, payload, Some(throwable))
}
