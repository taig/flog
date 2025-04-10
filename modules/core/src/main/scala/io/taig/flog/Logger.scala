package io.taig.flog

import cats.*
import cats.effect.*
import cats.effect.std.Queue
import cats.effect.syntax.all.*
import cats.syntax.all.*
import fs2.Stream
import io.taig.flog.data.*
import io.taig.flog.util.EventPrinter

import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

abstract class Logger[F[_]] extends LoggerLike[F]:
  def log(events: Long => List[Event]): F[Unit]
  final def mapK[G[_]](fk: F ~> G): Logger[G] = event => fk(log(event))

object Logger:
  /** Create a basic `Logger` that executes the given `write` function when an `Event` is received
    *
    * This `Logger` performs no additional actions after evaluating the given timestamp, building the `Event` and
    * forwarding it to the `write` function. It is therefore the callers responsibility to take care of asynchronicity
    * and thread safety.
    *
    * Use `Logger.apply[F]` to get a timestamp that will automatically use the current time from `Clock[F]`.
    */
  def raw[F[_]](timestamp: F[Long], write: List[Event] => F[Unit])(using F: Monad[F]): Logger[F] = f =>
    timestamp.flatMap: timestamp =>
      val events = f(timestamp)
      if events.isEmpty then F.unit else write(events)

  /** Create a basic `Logger` that executes the given `write` function when an `Event` is received
    *
    * This `Logger` performs no additional actions after evaluating the given timestamp, building the `Event` and
    * forwarding it to the `write` function. It is therefore the callers responsibility to take care of asynchronicity
    * and thread safety.
    *
    * Use `Logger.raw[F]` to provide a custom timestamp that does not require an instance of `Clock[F]`.
    */
  def apply[F[_]: Monad](write: List[Event] => F[Unit])(using clock: Clock[F]): Logger[F] =
    raw[F](clock.realTime.map(_.toMillis), write)

  def noTimestamp[F[_]](write: List[Event] => F[Unit])(using F: Applicative[F]): Logger[F] = f =>
    val events = f(-1)
    if events.isEmpty then F.unit else write(events)

  def list[F[_]: Monad: Clock](target: Ref[F, List[Event]]): Logger[F] = Logger[F](events => target.update(events ++ _))

  def unsafeOutput[F[_]](target: OutputStream, buffer: Int)(using F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))
      .map: writer =>
        Logger[F]: events =>
          F.delay:
            events.foreach(event => writer.write(EventPrinter(event)))
            writer.flush()

  def output[F[_]](target: OutputStream, buffer: Int)(using F: Sync[F]): Resource[F, Logger[F]] =
    Resource.fromAutoCloseable(F.delay(target)).evalMap(unsafeOutput(_, buffer))

  def stdOut[F[_]: Sync](buffer: Int): F[Logger[F]] = unsafeOutput(System.out, buffer)

  def stdOut[F[_]: Sync]: F[Logger[F]] = stdOut[F](buffer = 1024)

  /** Write all logs into a `Queue` and process them asynchronously
    *
    * The `timestamp` is used to set the `Event` time when it is added to the queue, the underlying `Logger`'s
    * `timestamp` is discarded.
    */
  def queued[F[_]: Concurrent](timestamp: F[Long], logger: Logger[F]): Resource[F, Logger[F]] = Resource
    .eval(Queue.unbounded[F, Option[Event]])
    .flatMap: queue =>
      val enqueue = raw[F](timestamp, _.traverse_(event => queue.offer(event.some)))
      val process = Stream
        .fromQueueNoneTerminated(queue)
        .chunks
        .evalMap(events => logger.log(_ => events.toList))
        .compile
        .drain

      Resource.make(process.start)(fiber => queue.offer(none) *> fiber.join.void).as(enqueue)

  /** Write all logs into a `Queue` and process them asynchronously */
  def queued[F[_]: Concurrent](logger: Logger[F])(using clock: Clock[F]): Resource[F, Logger[F]] =
    queued[F](clock.realTime.map(_.toMillis), logger)

  /** Write all logs into a `Queue` and process them asynchronously as soon as at least `buffer` logs have accumulated
    *
    * The `timestamp` is used to set the `Event` time when it is added to the queue, the underlying `Logger`'s
    * `timestamp` is discarded.
    */
  def batched[F[_]: Concurrent](timestamp: F[Long], logger: Logger[F], buffer: Int): Resource[F, Logger[F]] = Resource
    .eval(Queue.unbounded[F, Option[Event]])
    .flatMap: queue =>
      val enqueue = raw[F](timestamp, _.traverse_(event => queue.offer(event.some)))
      val process = Stream
        .fromQueueNoneTerminated(queue)
        .chunkN(buffer, allowFewer = true)
        .evalMap(events => logger.log(_ => events.toList))
        .compile
        .drain

      Resource.make(process.start)(fiber => queue.offer(none) *> fiber.join.void).as(enqueue)

  /** Write all logs into a `Queue` and process them asynchronously as soon as at least `buffer` logs have accumulated
    */
  def batched[F[_]: Concurrent](logger: Logger[F], buffer: Int)(using clock: Clock[F]): Resource[F, Logger[F]] =
    batched(clock.realTime.map(_.toMillis), logger, buffer)

  /** Forward logs to all given loggers */
  def broadcast[F[_]: Monad](loggers: List[Logger[F]])(using clock: Clock[F]): Logger[F] =
    if loggers.isEmpty then noop[F]
    else
      val timestamp = clock.realTime.map(_.toMillis)

      new Logger[F]:
        override def log(events: Long => List[Event]): F[Unit] = timestamp.flatMap: timestamp =>
          val broadcastEvents = events(timestamp)
          loggers.traverse_(_.log(_ => broadcastEvents))

  /** This `Logger` does nothing */
  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit

  implicit class Ops[F[_]](logger: Logger[F]) extends LoggerOps[Logger, F]:
    override def modify(f: List[Event] => List[Event]): Logger[F] =
      events => logger.log(timestamp => f(events(timestamp)))
