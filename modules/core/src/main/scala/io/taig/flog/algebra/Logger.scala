package io.taig.flog.algebra

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.util.concurrent.TimeUnit

import cats._
import cats.effect.{Clock, Concurrent, Resource, Sync}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.JsonObject
import io.taig.flog.data.{Event, Level, Scope}
import io.taig.flog.util.Printer

abstract class Logger[F[_]] {
  def log(event: Long => Event): F[Unit]

  final def mapK[G[_]](fk: F ~> G): Logger[G] = event => fk(log(event))

  def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    log(Event(_, level, scope, message, payload, throwable))

  final def debug(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Debug, scope, message, payload, throwable)

  final def error(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Error, scope, message, payload, throwable)

  final def info(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Info, scope, message, payload, throwable)

  final def warning(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Warning, scope, message, payload, throwable)
}

object Logger {

  /** Create a basic `Logger` that executes the given `write` function when
    * an `Event` is received
    *
    * This `Logger` performs no additional actions after evaluating the given
    * timestamp, building the `Event` and forwarding it to the `write` function.
    * It is therefore the callers responsibility to take care of asynchronicity
    * and thread safety.
    *
    * Use `Logger.apply[F]` to get a timestamp that will automatically use
    * the current time from `Clock[F]`.
    */
  def raw[F[_]: FlatMap](
      timestamp: F[Long],
      write: Event => F[Unit]
  ): Logger[F] =
    event => timestamp.flatMap(timestamp => write(event(timestamp)))

  /** Create a basic `Logger` that executes the given `write` function when
    * an `Event` is received
    *
    * This `Logger` performs no additional actions after evaluating the given
    * timestamp, building the `Event` and forwarding it to the `write` function.
    * It is therefore the callers responsibility to take care of asynchronicity
    * and thread safety.
    *
    * Use `Logger.raw[F]` to provide a custom timestamp that does not require an
    * instance of `Clock[F]`.
    */
  def apply[F[_]: FlatMap](
      write: Event => F[Unit]
  )(implicit clock: Clock[F]): Logger[F] =
    raw[F](clock.realTime(TimeUnit.MILLISECONDS), write)

  def output[F[_]: Clock](
      target: OutputStream,
      buffer: Int,
      close: Boolean = true
  )(implicit F: Sync[F]): Resource[F, Logger[F]] = {
    val acquire =
      F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))
    val release = (target: BufferedWriter) =>
      if (close) F.delay(target.close()) else F.unit

    Resource.make(acquire)(release).map { writer =>
      Logger[F] { event =>
        F.delay {
          writer.write(Printer.event(event).show)
          writer.flush()
        }
      }
    }
  }

  def stdOut[F[_]: Concurrent: Clock](buffer: Int): Resource[F, Logger[F]] =
    output(System.out, buffer, close = false)

  def stdOut[F[_]: Concurrent: Clock]: Resource[F, Logger[F]] =
    stdOut[F](buffer = 1024)

  def queued[F[_]: Concurrent](
      timestamp: F[Long],
      logger: Logger[F]
  ): Resource[F, Logger[F]] =
    Resource.liftF(Queue.unbounded[F, Event]).flatMap { queue =>
      val enqueue = raw[F](timestamp, queue.enqueue1)
      val process = queue.dequeue.evalMap(event => logger.log(_ => event))
      (Stream.emit(enqueue) concurrently process).compile.resource.lastOrError
    }

  def queued[F[_]: Concurrent](
      logger: Logger[F]
  )(implicit clock: Clock[F]): Resource[F, Logger[F]] =
    queued[F](clock.realTime(TimeUnit.MILLISECONDS), logger)

  def broadcast[F[_]: Monad: Clock](loggers: Logger[F]*): Logger[F] =
    if (loggers.isEmpty) noop[F]
    else Logger[F](event => loggers.toList.traverse_(_.log(_ => event)))

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit
}
