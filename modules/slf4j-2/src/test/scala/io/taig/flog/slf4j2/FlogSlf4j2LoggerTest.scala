package io.taig.flog.slf4j2

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.taig.flog.Logger
import io.taig.flog.data.Event
import io.taig.flog.data.Level
import munit.CatsEffectSuite
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicReference

final class FlogSlf4j2LoggerTest extends CatsEffectSuite:
  private val slf4j = LoggerFactory.getLogger("test")

  private val marker = MarkerFactory.getMarker("marker")

  private def bridge(logger: Logger[IO], level: Level = Level.Debug): Resource[IO, Unit] =
    Dispatcher.sequential[IO].flatMap(FlogSlf4j2Logger.initializeIO(_)(logger, level))

  /** `isXEnabled` for trace, debug, info, warn and error, without and with a `Marker` */
  private val enabled: IO[(List[Boolean], List[Boolean])] = IO.blocking:
    (
      List(
        slf4j.isTraceEnabled(),
        slf4j.isDebugEnabled(),
        slf4j.isInfoEnabled(),
        slf4j.isWarnEnabled(),
        slf4j.isErrorEnabled()
      ),
      List(
        slf4j.isTraceEnabled(marker),
        slf4j.isDebugEnabled(marker),
        slf4j.isInfoEnabled(marker),
        slf4j.isWarnEnabled(marker),
        slf4j.isErrorEnabled(marker)
      )
    )

  /** Run `f` against the SLF4J API and return everything it wrote to `System.err` */
  private def stdErrOf(f: org.slf4j.Logger => Unit): IO[String] = IO.blocking:
    val buffer = new ByteArrayOutputStream()
    val original = System.err
    System.setErr(new PrintStream(buffer, true))
    try f(slf4j)
    finally System.setErr(original)
    buffer.toString

  /** Run `f` against the SLF4J API while the bridge forwards into a fresh `Logger.list` and return the received events
    * in order
    */
  private def eventsOf(level: Level = Level.Debug)(f: org.slf4j.Logger => Unit): IO[List[Event]] = Ref[IO]
    .of(List.empty[Event])
    .flatTap: target =>
      bridge(Logger.list[IO](target), level).surround(IO.blocking(f(slf4j)))
    .flatMap(_.get.map(_.reverse))

  test("marker overloads log at their own level"):
    assertIO(
      obtained = eventsOf(): logger =>
        logger.error(marker, "error")
        logger.warn(marker, "warn")
        logger.info(marker, "info")
        logger.debug(marker, "debug")
        logger.trace(marker, "trace")
      .map(_.map(_.level)),
      returns = List(Level.Error, Level.Warning, Level.Info, Level.Debug, Level.Debug)
    )

  test("logging after the active bridge was released prints nothing"):
    (bridge(Logger.noop[IO]).allocated, bridge(Logger.noop[IO]).allocated).flatMapN:
      case ((_, releaseFirst), (_, releaseSecond)) =>
        releaseSecond *> assertIO(obtained = stdErrOf(_.debug("hello")), returns = "").guarantee(releaseFirst)

  test("releasing an older bridge keeps the newer one active"):
    Ref[IO]
      .of(List.empty[Event])
      .flatMap: target =>
        (bridge(Logger.noop[IO]).allocated, bridge(Logger.list[IO](target)).allocated).flatMapN:
          case ((_, releaseFirst), (_, releaseSecond)) =>
            val logAfterStaleRelease = releaseFirst *> IO.blocking(slf4j.info("hello")) *>
              assertIO(obtained = target.get.map(_.map(_.message)), returns = List("hello"))
            logAfterStaleRelease.guarantee(releaseSecond)

  test("logging without an active bridge prints nothing"):
    assertIO(obtained = stdErrOf(_.error("hello")), returns = "")

  test("levels below the minimum level are disabled"):
    val expected = List(false, false, true, true, true)
    assertIO(obtained = bridge(Logger.noop[IO], Level.Info).surround(enabled), returns = (expected, expected))

  test("messages below the minimum level are not forwarded"):
    assertIO(
      obtained = eventsOf(Level.Info): logger =>
        logger.trace("trace")
        logger.debug(marker, "debug")
        logger.info("info")
      .map(_.map(_.message)),
      returns = List("info")
    )

  test("a bridge that is released while forwarding a message prints nothing"):
    val release = new AtomicReference[IO[Unit]](IO.unit)
    val unsafeRun: IO[Unit] => Unit =
      _ => (release.get() *> IO.raiseError(new IllegalStateException("Dispatcher already closed"))).unsafeRunSync()
    FlogSlf4j2Logger
      .initializeF(unsafeRun)(Logger.noop[IO])
      .allocated
      .flatMap: (_, releaseBridge) =>
        IO(release.set(releaseBridge)) *> assertIO(obtained = stdErrOf(_.info("hello")), returns = "")
