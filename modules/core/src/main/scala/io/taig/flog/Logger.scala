package io.taig.flog

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.util.concurrent.TimeUnit

import cats._
import cats.effect.concurrent.Ref
import cats.effect.syntax.all._
import cats.effect.{Clock, Concurrent, Resource, Sync}
import cats.syntax.all._
import fs2.concurrent.Queue
import io.taig.flog.data._
import io.taig.flog.util.EventPrinter

abstract class Logger[F[_]] extends LoggerLike[Logger, F] { self =>
  final override def modify(f: List[Event] => List[Event]): Logger[F] = new Logger[F] {
    override def log(events: Long => List[Event]): F[Unit] = self.log(timestamp => f(events(timestamp)))
  }

  final def mapK[G[_]](fk: F ~> G): Logger[G] = event => fk(log(event))
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
  def raw[F[_]](timestamp: F[Long], write: List[Event] => F[Unit])(implicit F: Monad[F]): Logger[F] = f =>
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

  def noTimestamp[F[_]: Applicative](write: Event => F[Unit]): Logger[F] = _.apply(-1).traverse_(write)

  def list[F[_]: Monad: Clock](target: Ref[F, List[Event]]): Logger[F] = Logger[F](events => target.update(events ++ _))

  def unsafeOutput[F[_]: Clock](target: OutputStream, buffer: Int)(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer)).map { writer =>
      Logger[F] { events =>
        F.delay {
          events.foreach(event => writer.write(EventPrinter(event)))
          writer.flush()
        }
      }
    }

  def output[F[_]: Clock](target: OutputStream, buffer: Int)(implicit F: Sync[F]): Resource[F, Logger[F]] =
    Resource.fromAutoCloseable(F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))).map { writer =>
      Logger[F] { events =>
        F.delay {
          events.foreach(event => writer.write(EventPrinter(event)))
          writer.flush()
        }
      }
    }

  def stdOut[F[_]: Concurrent: Clock](buffer: Int): F[Logger[F]] = unsafeOutput(System.out, buffer)

  def stdOut[F[_]: Concurrent: Clock]: F[Logger[F]] = stdOut[F](buffer = 1024)

  def queued[F[_]: Concurrent](timestamp: F[Long], logger: Logger[F]): Resource[F, Logger[F]] =
    Resource.liftF(Queue.noneTerminated[F, Event]).flatMap { queue =>
      val enqueue = raw[F](timestamp, _.traverse_(event => queue.enqueue1(event.some)))
      val process = queue.dequeue.chunks.evalMap(events => logger.log(_ => events.toList))
      Resource.make(process.compile.drain.start)(fiber => queue.enqueue1(None) *> fiber.join).as(enqueue)
    }

  def queued[F[_]: Concurrent](logger: Logger[F])(implicit clock: Clock[F]): Resource[F, Logger[F]] =
    queued[F](clock.realTime(TimeUnit.MILLISECONDS), logger)

  /** Forward logs to all given loggers */
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

  /** This `Logger` does nothing */
  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit
}
