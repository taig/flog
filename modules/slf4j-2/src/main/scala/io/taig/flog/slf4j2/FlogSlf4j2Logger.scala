package io.taig.flog.slf4j2

import cats.effect.IO
import cats.effect.Resource
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

import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal

final class FlogSlf4j2Logger(name: String) extends Logger:
  override def getName(): String = name

  private def log(level: Level, msg: String, throwable: Option[Throwable]): Unit =
    val bridge = FlogSlf4j2Logger.active.get()
    if bridge.isEnabled(level) then
      try {
        bridge.log(level, msg, throwable)
      } catch {
        // A bridge that was released while forwarding fails because its resources are gone, which is expected
        case NonFatal(_) if FlogSlf4j2Logger.active.get() ne bridge => ()
        case NonFatal(cause)                                        =>
          System.err.print(
            s"Failed to handle slf4j log message: '$msg'\n" +
              throwable.map(StacktracePrinter.apply).orEmpty
          )
          System.err.print(StacktracePrinter(cause))
        case _ => ()
      }

  private def isEnabled(level: Level): Boolean = FlogSlf4j2Logger.active.get().isEnabled(level)

  override def isDebugEnabled(): Boolean = isEnabled(Level.Debug)
  override def isDebugEnabled(marker: Marker): Boolean = isDebugEnabled()
  override def isErrorEnabled(): Boolean = isEnabled(Level.Error)
  override def isErrorEnabled(marker: Marker): Boolean = isErrorEnabled()
  override def isInfoEnabled(): Boolean = isEnabled(Level.Info)
  override def isInfoEnabled(marker: Marker): Boolean = isInfoEnabled()
  override def isTraceEnabled(): Boolean = isEnabled(Level.Debug)
  override def isTraceEnabled(marker: Marker): Boolean = isTraceEnabled()
  override def isWarnEnabled(): Boolean = isEnabled(Level.Warning)
  override def isWarnEnabled(marker: Marker): Boolean = isWarnEnabled()

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
  override def error(marker: Marker, msg: String): Unit = error(msg, throwable = none)
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
  override def info(marker: Marker, msg: String): Unit = info(msg, throwable = none)
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
  override def trace(marker: Marker, msg: String): Unit = trace(msg, throwable = none)
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
  override def warn(marker: Marker, msg: String): Unit = warn(msg, throwable = none)
  override def warn(msg: String, obj: Object): Unit = warn(msg = s"$msg $obj", throwable = none)
  override def warn(msg: String, obj1: Object, obj2: Object): Unit =
    warn(msg = s"$msg $obj1, $obj2", throwable = none)
  override def warn(msg: String, objs: Object*): Unit = warn(msg = s"$msg ${objs.mkString(", ")}", throwable = none)
  override def warn(msg: String, t: Throwable): Unit = warn(msg, throwable = Option(t))
  override def warn(msg: String): Unit = warn(msg, throwable = none)

object FlogSlf4j2Logger:
  /** The currently installed forwarding target, compared by reference so that a released bridge only uninstalls itself
    */
  final private class Bridge(val isEnabled: Level => Boolean, val log: (Level, String, Option[Throwable]) => Unit)

  private val Noop: Bridge = new Bridge(_ => false, (_, _, _) => ())

  private val active: AtomicReference[Bridge] = new AtomicReference(Noop)

  /** Forward all SLF4J logs to the given `logger` while the `Resource` is in use
    *
    * SLF4J is global, so the most recently acquired bridge wins. On release, the bridge is uninstalled (and SLF4J logs
    * are discarded) only if it is still the active one, so releasing an older bridge does not clobber a newer one.
    */
  def initializeF[F[_]](unsafeRun: F[Unit] => Unit)(logger: FlogLogger[F], level: Level = Level.Debug)(using
      F: Sync[F]
  ): Resource[F, Unit] =
    val acquire = F.delay:
      val bridge = new Bridge(
        isEnabled = _ >= level,
        log = (messageLevel, msg, throwable) =>
          unsafeRun(logger.apply(messageLevel, Scope.Root, msg, JsonObject.empty, throwable))
      )
      active.set(bridge)
      bridge

    Resource.make(acquire)(bridge => F.delay(active.compareAndSet(bridge, Noop)).void).void

  def initializeIO(dispatcher: Dispatcher[IO])(logger: FlogLogger[IO], level: Level = Level.Debug): Resource[IO, Unit] =
    initializeF(unsafeRun = dispatcher.unsafeRunSync)(logger, level)

  @deprecated("Use initializeIO instead", "0.17.3")
  def initialize(dispatcher: Dispatcher[IO])(logger: FlogLogger[IO]): Resource[IO, Unit] =
    initializeIO(dispatcher)(logger)
