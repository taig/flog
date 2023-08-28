package io.taig.flog.slf4j2

import cats.effect.std.Dispatcher
import io.taig.flog.Logger
import io.taig.flog.data.{Level, Scope}
import org.slf4j.Marker
import org.slf4j.event.Level as Slf4jLevel
import org.slf4j.helpers.MessageFormatter

final class FlogSlf4j2Runtime[F[_]](logger: Logger[F], dispatcher: Dispatcher[F]) extends LoggerRuntime:
  override def log(
      name: String,
      level: Slf4jLevel,
      marker: Marker,
      messagePattern: String,
      arguments: Array[AnyRef],
      throwable: Throwable
  ): Unit =
    val scope = Scope.fromName(name)

    val message =
      if arguments != null
      then MessageFormatter.arrayFormat(messagePattern, arguments).getMessage
      else messagePattern

    try dispatcher.unsafeRunAndForget(logger.apply(toLevel(level), scope, message, throwable = Option(throwable)))
    catch
      case exception: IllegalStateException if exception.getMessage == "dispatcher already shutdown" => ()
      case throwable: Throwable                                                                      => throw throwable

  def toLevel(level: Slf4jLevel): Level = level match
    case Slf4jLevel.ERROR => Level.Error
    case Slf4jLevel.WARN  => Level.Warning
    case Slf4jLevel.INFO  => Level.Info
    case Slf4jLevel.DEBUG => Level.Debug
    case Slf4jLevel.TRACE => Level.Info
