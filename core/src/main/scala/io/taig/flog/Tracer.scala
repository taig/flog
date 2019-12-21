package io.taig.flog

import java.util.UUID

import cats.effect.Sync
import cats.implicits._

/**
  * Provides an execution context with a `Logger` that carries a `UUID` tracing id
  */
abstract class Tracer[F[_]] {
  def run[A](f: Logger[F] => F[A]): F[A]
}

object Tracer {
  def uuid[F[_]](implicit F: Sync[F]): F[UUID] = F.delay(UUID.randomUUID())

  /**
    * Create a `Tracer` that automatically logs an unhandled error and then
    * rethrows it
    */
  def reporting[F[_]: Sync](logger: Logger[F]): Tracer[F] =
    new Tracer[F] {
      override def run[A](f: Logger[F] => F[A]): F[A] =
        uuid[F].flatMap { trace =>
          val tracer = logger.trace(trace)
          f(tracer).handleErrorWith { throwable =>
            tracer.error(throwable = throwable.some) *>
              TracedFailure(trace, logger.prefix, logger.presets, throwable)
                .raiseError[F, A]
          }
        }
    }

  /**
    * Create a `Tracer` that wraps an unhandled error in a `TracedFailure`
    */
  def adapting[F[_]: Sync](logger: Logger[F]): Tracer[F] =
    new Tracer[F] {
      override def run[A](f: Logger[F] => F[A]): F[A] =
        uuid[F].flatMap { trace =>
          val tracer = logger.trace(trace)
          f(tracer).adaptErr {
            case throwable =>
              TracedFailure(trace, logger.prefix, logger.presets, throwable)
          }
        }
    }
}
