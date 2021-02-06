package io.taig.flog

import io.taig.flog.data.{Event, Level, Payload, Scope}

trait LoggerLike[F[_]] { this: Logger[F] =>
  final def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => Payload.Object = Payload.Empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = log(timestamp => List(Event(timestamp, level, scope, message, payload, throwable)))

  final def debug(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Debug, scope, message, payload, throwable)

  final def debug(message: String): F[Unit] = debug(Scope.Root, message, Payload.Empty, None)
  final def debug(scope: Scope, message: String): F[Unit] = debug(scope, message, Payload.Empty, None)
  final def debug(payload: => Payload.Object): F[Unit] = debug(Scope.Root, "", payload, None)
  final def debug(scope: Scope, payload: => Payload.Object): F[Unit] = debug(scope, "", payload, None)
  final def debug(message: String, payload: => Payload.Object): F[Unit] = debug(Scope.Root, message, payload, None)
  final def debug(scope: Scope, message: String, payload: => Payload.Object): F[Unit] =
    debug(scope, message, payload, None)
  final def debug(message: String, throwable: Throwable): F[Unit] =
    debug(Scope.Root, message, Payload.Empty, Some(throwable))
  final def debug(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    debug(scope, message, Payload.Empty, Some(throwable))
  final def debug(message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    debug(Scope.Root, message, payload, Some(throwable))
  final def debug(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    debug(scope, message, payload, Some(throwable))

  final def error(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Error, scope, message, payload, throwable)

  final def error(message: String): F[Unit] = error(Scope.Root, message, Payload.Empty, None)
  final def error(scope: Scope, message: String): F[Unit] = error(scope, message, Payload.Empty, None)
  final def error(payload: => Payload.Object): F[Unit] = error(Scope.Root, "", payload, None)
  final def error(scope: Scope, payload: => Payload.Object): F[Unit] = error(scope, "", payload, None)
  final def error(message: String, payload: => Payload.Object): F[Unit] = error(Scope.Root, message, payload, None)
  final def error(scope: Scope, message: String, payload: => Payload.Object): F[Unit] =
    error(scope, message, payload, None)
  final def error(message: String, throwable: Throwable): F[Unit] =
    error(Scope.Root, message, Payload.Empty, Some(throwable))
  final def error(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    error(scope, message, Payload.Empty, Some(throwable))
  final def error(message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    error(Scope.Root, message, payload, Some(throwable))
  final def error(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    error(scope, message, payload, Some(throwable))

  final def info(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Info, scope, message, payload, throwable)

  final def info(message: String): F[Unit] = info(Scope.Root, message, Payload.Empty, None)
  final def info(scope: Scope, message: String): F[Unit] = info(scope, message, Payload.Empty, None)
  final def info(payload: => Payload.Object): F[Unit] = info(Scope.Root, "", payload, None)
  final def info(scope: Scope, payload: => Payload.Object): F[Unit] = info(scope, "", payload, None)
  final def info(message: String, payload: => Payload.Object): F[Unit] = info(Scope.Root, message, payload, None)
  final def info(scope: Scope, message: String, payload: => Payload.Object): F[Unit] =
    info(scope, message, payload, None)
  final def info(message: String, throwable: Throwable): F[Unit] =
    info(Scope.Root, message, Payload.Empty, Some(throwable))
  final def info(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    info(scope, message, Payload.Empty, Some(throwable))
  final def info(message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    info(Scope.Root, message, payload, Some(throwable))
  final def info(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    info(scope, message, payload, Some(throwable))

  final def warning(
      scope: Scope,
      message: String,
      payload: => Payload.Object,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Warning, scope, message, payload, throwable)

  final def warning(message: String): F[Unit] = warning(Scope.Root, message, Payload.Empty, None)
  final def warning(scope: Scope, message: String): F[Unit] = warning(scope, message, Payload.Empty, None)
  final def warning(payload: => Payload.Object): F[Unit] = warning(Scope.Root, "", payload, None)
  final def warning(scope: Scope, payload: => Payload.Object): F[Unit] = warning(scope, "", payload, None)
  final def warning(message: String, payload: => Payload.Object): F[Unit] = warning(Scope.Root, message, payload, None)
  final def warning(scope: Scope, message: String, payload: => Payload.Object): F[Unit] =
    warning(scope, message, payload, None)
  final def warning(message: String, throwable: Throwable): F[Unit] =
    warning(Scope.Root, message, Payload.Empty, Some(throwable))
  final def warning(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    warning(scope, message, Payload.Empty, Some(throwable))
  final def warning(message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    warning(Scope.Root, message, payload, Some(throwable))
  final def warning(scope: Scope, message: String, payload: => Payload.Object, throwable: Throwable): F[Unit] =
    warning(scope, message, payload, Some(throwable))
}
