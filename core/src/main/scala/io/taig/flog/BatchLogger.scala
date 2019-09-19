package io.taig.flog

import cats.Monad
import cats.effect.concurrent.Semaphore
import cats.effect.implicits._
import cats.effect._
import cats.implicits._

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/**
  * Delay logging by a fixed interval, accumulating all events and submitting
  * them in one batch when the delay passed
  *
  * This `Logger` is useful when dealing with expensive `Loggers`, e.g.
  * the `SheetsLogger` that performs a network request to submit events.
  */
final class BatchLogger[F[_]: Sync](enqueue: List[Event] => F[Unit])
    extends SyncLogger[F] {
  override def apply(events: List[Event]): F[Unit] = enqueue(events)
}

object BatchLogger {
  def apply[F[_]: ContextShift: Timer](
      logger: Logger[F]
  )(
      interval: FiniteDuration
  )(implicit F: Concurrent[F]): Resource[F, Logger[F]] = {
    val buffer = mutable.ListBuffer.empty[Event]
    val sleep = Timer[F].sleep(interval)

    Resource.liftF(Semaphore[F](1)).flatMap { lock =>
      val enqueue = (events: List[Event]) =>
        lock.withPermit(F.delay(buffer.appendAll(events)))
      val dequeue = lock.withPermit(extract[F](buffer)) >>= logger.apply

      Resource
        .make(repeat(sleep >> dequeue).start)(_.cancel *> dequeue)
        .as(new BatchLogger[F](enqueue(_).void))
    }
  }

  private def extract[F[_]](
      buffer: mutable.ListBuffer[Event]
  )(implicit F: Sync[F]): F[List[Event]] =
    F.delay(buffer.toList) <* F.delay(buffer.clear())

  private def repeat[F[_]: Monad, A](
      program: F[A]
  )(implicit context: ContextShift[F]): F[Unit] =
    for {
      _ <- program
      _ <- context.shift
      _ <- repeat(program)
    } yield ()
}
