package io.taig.flog.slf4j

import cats.effect.std.Dispatcher
import io.taig.flog.Logger
import io.taig.flog.data.Level
import org.slf4j.helpers.{FormattingTuple, MarkerIgnoringBase, MessageFormatter}

import java.util.Objects

final class FlogSlf4jLogger[F[_]](logger: Logger[F])(dispatcher: Dispatcher[F]) extends MarkerIgnoringBase {
  def log(level: Level, format: String, args: Array[AnyRef]): Unit =
    log(level, MessageFormatter.arrayFormat(format, args))

  def log(level: Level, message: String): Unit =
    log(level, message, null: Throwable)

  def log(level: Level, message: String, throwable: Throwable): Unit = dispatcher.unsafeRunSync {
    logger
      .apply(
        level,
        message = Objects.toString(message, ""),
        throwable = Option(throwable)
      )
  }

  def log(level: Level, result: FormattingTuple): Unit = dispatcher.unsafeRunSync {
    val message = Objects.toString(result.getMessage, "")
    val throwable = Option(result.getThrowable)
    logger.apply(level, message = message, throwable = throwable)
  }

  override val isTraceEnabled: Boolean = true

  override def trace(msg: String): Unit = log(Level.Debug, msg)

  override def trace(format: String, arg: AnyRef): Unit =
    log(Level.Debug, format, Array(arg))

  override def trace(format: String, arg1: AnyRef, arg2: AnyRef): Unit =
    log(Level.Debug, format, Array(arg1, arg2))

  override def trace(format: String, arguments: AnyRef*): Unit =
    log(Level.Debug, format, arguments.toArray)

  override def trace(msg: String, t: Throwable): Unit = log(Level.Debug, msg, t)

  override val isDebugEnabled: Boolean = true

  override def debug(msg: String): Unit = log(Level.Debug, msg)

  override def debug(format: String, arg: AnyRef): Unit =
    log(Level.Debug, format, Array(arg))

  override def debug(format: String, arg1: AnyRef, arg2: AnyRef): Unit =
    log(Level.Debug, format, Array(arg1, arg2))

  override def debug(format: String, arguments: AnyRef*): Unit =
    log(Level.Debug, format, arguments.toArray)

  override def debug(msg: String, t: Throwable): Unit = log(Level.Debug, msg, t)

  override val isInfoEnabled: Boolean = true

  override def info(msg: String): Unit = log(Level.Info, msg)

  override def info(format: String, arg: AnyRef): Unit =
    log(Level.Info, format, Array(arg))

  override def info(format: String, arg1: AnyRef, arg2: AnyRef): Unit =
    log(Level.Info, format, Array(arg1, arg2))

  override def info(format: String, arguments: AnyRef*): Unit =
    log(Level.Info, format, arguments.toArray)

  override def info(msg: String, t: Throwable): Unit = log(Level.Info, msg, t)

  override val isWarnEnabled: Boolean = true

  override def warn(msg: String): Unit = log(Level.Warning, msg)

  override def warn(format: String, arg: AnyRef): Unit =
    log(Level.Warning, format, Array(arg))

  override def warn(format: String, arg1: AnyRef, arg2: AnyRef): Unit =
    log(Level.Warning, format, Array(arg1, arg2))

  override def warn(format: String, arguments: AnyRef*): Unit =
    log(Level.Warning, format, arguments.toArray)

  override def warn(msg: String, t: Throwable): Unit =
    log(Level.Warning, msg, t)

  override val isErrorEnabled: Boolean = true

  override def error(msg: String): Unit = log(Level.Error, msg)

  override def error(format: String, arg: AnyRef): Unit =
    log(Level.Error, format, Array(arg))

  override def error(format: String, arg1: AnyRef, arg2: AnyRef): Unit =
    log(Level.Error, format, Array(arg1, arg2))

  override def error(format: String, arguments: AnyRef*): Unit =
    log(Level.Error, format, arguments.toArray)

  override def error(msg: String, t: Throwable): Unit = log(Level.Error, msg, t)
}
