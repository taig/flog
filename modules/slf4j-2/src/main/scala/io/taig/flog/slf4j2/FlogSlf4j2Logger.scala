package io.taig.flog.slf4j2

import cats.effect.IO
import cats.effect.Sync
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.circe.JsonObject
import io.taig.flog.Logger as FlogLogger
import io.taig.flog.data.Level
import io.taig.flog.data.Scope
import io.taig.flog.util.StacktracePrinter
import org.slf4j.Logger
import org.slf4j.Marker

import scala.util.control.NonFatal

final class FlogSlf4j2Logger(name: String) extends Logger:
  override def getName(): String = name

  private def log(level: Level, msg: String, throwable: Option[Throwable]): Unit =
    try {
      FlogSlf4j2Logger.unsafeLog(level, msg, throwable)
    } catch {
      case NonFatal(cause) =>
        System.err.print(
          s"Failed to handle slf4j log message: '$msg'\n" +
            throwable.map(StacktracePrinter.apply).orEmpty
        )
        System.err.print(StacktracePrinter(cause))
      case _ => ()
    }

  override def isDebugEnabled(): Boolean = true
  override def isDebugEnabled(marker: Marker): Boolean = true
  override def isErrorEnabled(): Boolean = true
  override def isErrorEnabled(marker: Marker): Boolean = true
  override def isInfoEnabled(): Boolean = true
  override def isInfoEnabled(marker: Marker): Boolean = true
  override def isTraceEnabled(): Boolean = true
  override def isTraceEnabled(marker: Marker): Boolean = true
  override def isWarnEnabled(): Boolean = true
  override def isWarnEnabled(marker: Marker): Boolean = true

  def debug(msg: String, throwable: Option[Throwable]): Unit = log(level = Level.Debug, msg, throwable)
  def error(msg: String, throwable: Option[Throwable]): Unit = log(level = Level.Error, msg, throwable)
  def info(msg: String, throwable: Option[Throwable]): Unit = log(level = Level.Info, msg, throwable)
  def warn(msg: String, throwable: Option[Throwable]): Unit = log(level = Level.Warning, msg, throwable)
  def trace(msg: String, throwable: Option[Throwable]): Unit = log(level = Level.Debug, msg, throwable)

  override def debug(marker: Marker, msg: String, obj: Object): Unit = debug(msg = s"$msg, $obj", throwable = none)
  override def debug(marker: Marker, msg: String, obj1: Object, obj2: Object): Unit =
    debug(msg = s"$msg, $obj1, $obj2", throwable = none)
  override def debug(marker: Marker, msg: String, objs: Object*): Unit =
    debug(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def debug(marker: Marker, msg: String, t: Throwable): Unit = debug(msg, throwable = Option(t))
  override def debug(marker: Marker, msg: String): Unit = debug(msg, throwable = none)
  override def debug(msg: String, obj: Object): Unit = debug(msg = s"$msg $obj", throwable = none)
  override def debug(msg: String, obj1: Object, obj2: Object): Unit =
    debug(msg = s"$msg $obj1, $obj2", throwable = none)
  override def debug(msg: String, objs: Object*): Unit = debug(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def debug(msg: String, t: Throwable): Unit = debug(msg, throwable = Option(t))
  override def debug(msg: String): Unit = debug(msg, throwable = none)

  override def error(marker: Marker, msg: String, obj: Object): Unit = error(msg = s"$msg, $obj", throwable = none)
  override def error(marker: Marker, msg: String, obj1: Object, obj2: Object): Unit =
    error(msg = s"$msg, $obj1, $obj2", throwable = none)
  override def error(marker: Marker, msg: String, objs: Object*): Unit =
    error(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def error(marker: Marker, msg: String, t: Throwable): Unit = error(msg, throwable = Option(t))
  override def error(marker: Marker, msg: String): Unit = debug(msg, throwable = none)
  override def error(msg: String, obj: Object): Unit = error(msg = s"$msg $obj", throwable = none)
  override def error(msg: String, obj1: Object, obj2: Object): Unit =
    error(msg = s"$msg $obj1, $obj2", throwable = none)
  override def error(msg: String, objs: Object*): Unit = error(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def error(msg: String, t: Throwable): Unit = error(msg, throwable = Option(t))
  override def error(msg: String): Unit = error(msg, throwable = none)

  override def info(marker: Marker, msg: String, obj: Object): Unit = info(msg = s"$msg, $obj", throwable = none)
  override def info(marker: Marker, msg: String, obj1: Object, obj2: Object): Unit =
    info(msg = s"$msg, $obj1, $obj2", throwable = none)
  override def info(marker: Marker, msg: String, objs: Object*): Unit =
    info(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def info(marker: Marker, msg: String, t: Throwable): Unit = info(msg, throwable = Option(t))
  override def info(marker: Marker, msg: String): Unit = debug(msg, throwable = none)
  override def info(msg: String, obj: Object): Unit = info(msg = s"$msg $obj", throwable = none)
  override def info(msg: String, obj1: Object, obj2: Object): Unit =
    info(msg = s"$msg $obj1, $obj2", throwable = none)
  override def info(msg: String, objs: Object*): Unit = info(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def info(msg: String, t: Throwable): Unit = info(msg, throwable = Option(t))
  override def info(msg: String): Unit = info(msg, throwable = none)

  override def trace(marker: Marker, msg: String, obj: Object): Unit = trace(msg = s"$msg, $obj", throwable = none)
  override def trace(marker: Marker, msg: String, obj1: Object, obj2: Object): Unit =
    trace(msg = s"$msg, $obj1, $obj2", throwable = none)
  override def trace(marker: Marker, msg: String, objs: Object*): Unit =
    trace(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def trace(marker: Marker, msg: String, t: Throwable): Unit = trace(msg, throwable = Option(t))
  override def trace(marker: Marker, msg: String): Unit = debug(msg, throwable = none)
  override def trace(msg: String, obj: Object): Unit = trace(msg = s"$msg $obj", throwable = none)
  override def trace(msg: String, obj1: Object, obj2: Object): Unit =
    trace(msg = s"$msg $obj1, $obj2", throwable = none)
  override def trace(msg: String, objs: Object*): Unit = trace(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def trace(msg: String, t: Throwable): Unit = trace(msg, throwable = Option(t))
  override def trace(msg: String): Unit = trace(msg, throwable = none)

  override def warn(marker: Marker, msg: String, obj: Object): Unit = warn(msg = s"$msg, $obj", throwable = none)
  override def warn(marker: Marker, msg: String, obj1: Object, obj2: Object): Unit =
    warn(msg = s"$msg, $obj1, $obj2", throwable = none)
  override def warn(marker: Marker, msg: String, objs: Object*): Unit =
    warn(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def warn(marker: Marker, msg: String, t: Throwable): Unit = warn(msg, throwable = Option(t))
  override def warn(marker: Marker, msg: String): Unit = debug(msg, throwable = none)
  override def warn(msg: String, obj: Object): Unit = warn(msg = s"$msg $obj", throwable = none)
  override def warn(msg: String, obj1: Object, obj2: Object): Unit =
    warn(msg = s"$msg $obj1, $obj2", throwable = none)
  override def warn(msg: String, objs: Object*): Unit = warn(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def warn(msg: String, t: Throwable): Unit = warn(msg, throwable = Option(t))
  override def warn(msg: String): Unit = warn(msg, throwable = none)

object FlogSlf4j2Logger:
  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var unsafeLog: (Level, String, Option[Throwable]) => Unit = null

  def initializeF[F[_]](unsafeRun: F[Unit] => Unit)(logger: FlogLogger[F])(using F: Sync[F]): F[Unit] = F.delay:
    this.unsafeLog = (level: Level, msg: String, throwable: Option[Throwable]) =>
      unsafeRun(logger.apply(level, Scope.Root, msg, JsonObject.empty, throwable))

  def initializeIO(dispatcher: Dispatcher[IO])(logger: FlogLogger[IO]): IO[Unit] =
    initializeF(unsafeRun = dispatcher.unsafeRunSync)(logger)

  @deprecated("Use initializeIO instead", "0.17.3")
  def initialize(dispatcher: Dispatcher[IO])(logger: FlogLogger[IO]): IO[Unit] = initializeIO(dispatcher)(logger)
