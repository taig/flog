package io.taig.flog

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.util.concurrent.TimeUnit

import cats._
import cats.effect.{Clock, Concurrent, Resource, Sync}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.JsonObject
import io.taig.flog.data.{Context, Event, Level, Scope}
import io.taig.flog.internal.Builders
import io.taig.flog.util.Printer

abstract class Logger[F[_]] {
  def log(events: Long => List[Event]): F[Unit]

  final def mapK[G[_]](fk: F ~> G): Logger[G] = event => fk(log(event))

  def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = log { timestamp => List(Event(timestamp, level, scope, message, payload, throwable)) }

  final def debug(
      scope: Scope,
      message: String,
      payload: => JsonObject,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Debug, scope, message, payload, throwable)

  final def debug(message: String): F[Unit] = debug(Scope.Root, message, JsonObject.empty, None)
  final def debug(scope: Scope, message: String): F[Unit] = debug(scope, message, JsonObject.empty, None)
  final def debug(payload: => JsonObject): F[Unit] = debug(Scope.Root, "", payload, None)
  final def debug(scope: Scope, payload: => JsonObject): F[Unit] = debug(scope, "", payload, None)
  final def debug(message: String, payload: => JsonObject): F[Unit] = debug(Scope.Root, message, payload, None)
  final def debug(scope: Scope, message: String, payload: => JsonObject): F[Unit] = debug(scope, message, payload, None)
  final def debug(message: String, throwable: Throwable): F[Unit] =
    debug(Scope.Root, message, JsonObject.empty, Some(throwable))
  final def debug(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    debug(scope, message, JsonObject.empty, Some(throwable))
  final def debug(message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    debug(Scope.Root, message, payload, Some(throwable))
  final def debug(scope: Scope, message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    debug(scope, message, payload, Some(throwable))

  final def error(
      scope: Scope,
      message: String,
      payload: => JsonObject,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Error, scope, message, payload, throwable)

  final def error(message: String): F[Unit] = error(Scope.Root, message, JsonObject.empty, None)
  final def error(scope: Scope, message: String): F[Unit] = error(scope, message, JsonObject.empty, None)
  final def error(payload: => JsonObject): F[Unit] = error(Scope.Root, "", payload, None)
  final def error(scope: Scope, payload: => JsonObject): F[Unit] = error(scope, "", payload, None)
  final def error(message: String, payload: => JsonObject): F[Unit] = error(Scope.Root, message, payload, None)
  final def error(scope: Scope, message: String, payload: => JsonObject): F[Unit] = error(scope, message, payload, None)
  final def error(message: String, throwable: Throwable): F[Unit] =
    error(Scope.Root, message, JsonObject.empty, Some(throwable))
  final def error(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    error(scope, message, JsonObject.empty, Some(throwable))
  final def error(message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    error(Scope.Root, message, payload, Some(throwable))
  final def error(scope: Scope, message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    error(scope, message, payload, Some(throwable))

  final def info(
      scope: Scope,
      message: String,
      payload: => JsonObject,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Info, scope, message, payload, throwable)

  final def info(message: String): F[Unit] = info(Scope.Root, message, JsonObject.empty, None)
  final def info(scope: Scope, message: String): F[Unit] = info(scope, message, JsonObject.empty, None)
  final def info(payload: => JsonObject): F[Unit] = info(Scope.Root, "", payload, None)
  final def info(scope: Scope, payload: => JsonObject): F[Unit] = info(scope, "", payload, None)
  final def info(message: String, payload: => JsonObject): F[Unit] = info(Scope.Root, message, payload, None)
  final def info(scope: Scope, message: String, payload: => JsonObject): F[Unit] = info(scope, message, payload, None)
  final def info(message: String, throwable: Throwable): F[Unit] =
    info(Scope.Root, message, JsonObject.empty, Some(throwable))
  final def info(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    info(scope, message, JsonObject.empty, Some(throwable))
  final def info(message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    info(Scope.Root, message, payload, Some(throwable))
  final def info(scope: Scope, message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    info(scope, message, payload, Some(throwable))

  final def warning(
      scope: Scope,
      message: String,
      payload: => JsonObject,
      throwable: Option[Throwable]
  ): F[Unit] = apply(Level.Warning, scope, message, payload, throwable)

  final def warning(message: String): F[Unit] = warning(Scope.Root, message, JsonObject.empty, None)
  final def warning(scope: Scope, message: String): F[Unit] = warning(scope, message, JsonObject.empty, None)
  final def warning(payload: => JsonObject): F[Unit] = warning(Scope.Root, "", payload, None)
  final def warning(scope: Scope, payload: => JsonObject): F[Unit] = warning(scope, "", payload, None)
  final def warning(message: String, payload: => JsonObject): F[Unit] = warning(Scope.Root, message, payload, None)
  final def warning(scope: Scope, message: String, payload: => JsonObject): F[Unit] =
    warning(scope, message, payload, None)
  final def warning(message: String, throwable: Throwable): F[Unit] =
    warning(Scope.Root, message, JsonObject.empty, Some(throwable))
  final def warning(scope: Scope, message: String, throwable: Throwable): F[Unit] =
    warning(scope, message, JsonObject.empty, Some(throwable))
  final def warning(message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    warning(Scope.Root, message, payload, Some(throwable))
  final def warning(scope: Scope, message: String, payload: => JsonObject, throwable: Throwable): F[Unit] =
    warning(scope, message, payload, Some(throwable))
}

object Logger extends Builders[Logger] {

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
  def raw[F[_]](timestamp: F[Long], write: List[Event] => F[Unit])(implicit F: Monad[F]): Logger[F] =
    f =>
      timestamp.flatMap { timestamp =>
        val events = f(timestamp)
        F.whenA(events.nonEmpty)(write(events))
      }

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
  def apply[F[_]: Monad](write: List[Event] => F[Unit])(implicit clock: Clock[F]): Logger[F] =
    raw[F](clock.realTime(TimeUnit.MILLISECONDS), write)

  def noTimestamp[F[_]: Applicative](write: Event => F[Unit]): Logger[F] =
    _.apply(-1).traverse_(write)

  def unsafeOutput[F[_]: Clock](target: OutputStream, buffer: Int)(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer)).map { writer =>
      Logger[F] { events =>
        F.delay {
          events.foreach { event => writer.write(Printer.event(event).show) }

          writer.flush()
        }
      }
    }

  def output[F[_]: Clock](target: OutputStream, buffer: Int)(implicit F: Sync[F]): Resource[F, Logger[F]] = {
    val acquire = F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))
    val release = (target: BufferedWriter) => F.delay(target.close())

    Resource.make(acquire)(release).map { writer =>
      Logger[F] { events =>
        F.delay {
          events.foreach { event => writer.write(Printer.event(event).show) }

          writer.flush()
        }
      }
    }
  }

  def stdOut[F[_]: Concurrent: Clock](buffer: Int): F[Logger[F]] = unsafeOutput(System.out, buffer)

  def stdOut[F[_]: Concurrent: Clock]: F[Logger[F]] = stdOut[F](buffer = 1024)

  def queued[F[_]: Concurrent](timestamp: F[Long], logger: Logger[F]): Resource[F, Logger[F]] =
    Resource.liftF(Queue.unbounded[F, Event]).flatMap { queue =>
      val enqueue = raw[F](timestamp, _.traverse_(queue.enqueue1))
      val process = queue.dequeue.chunks.evalMap { events => logger.log(_ => events.toList) }
      (Stream.emit(enqueue) concurrently process).compile.resource.lastOrError
    }

  def queued[F[_]: Concurrent](logger: Logger[F])(implicit clock: Clock[F]): Resource[F, Logger[F]] =
    queued[F](clock.realTime(TimeUnit.MILLISECONDS), logger)

  def broadcast[F[_]: Monad](loggers: List[Logger[F]])(implicit clock: Clock[F]): Logger[F] =
    if (loggers.isEmpty) noop[F]
    else {
      val timestamp = clock.realTime(TimeUnit.MILLISECONDS)

      new Logger[F] {
        override def log(events: Long => List[Event]): F[Unit] =
          timestamp.flatMap { timestamp =>
            val broadcastEvents = events(timestamp)
            loggers.traverse_(_.log(_ => broadcastEvents))
          }
      }
    }

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit

  override def filter[F[_]](filter: Event => Boolean)(logger: Logger[F]): Logger[F] =
    events => logger.log(events(_).filter(filter))

  override def defaults[F[_]](context: Context)(logger: Logger[F]): Logger[F] =
    events => logger.log(events.apply(_).map(_.defaults(context)))
}
