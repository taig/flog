package io.taig.flog

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.UUIDs

/**
  * Provides an execution context with a `Logger` that carries a `UUID` tracing id
  */
abstract class Tracer[F[_]] {
  def run[A](f: Logger[F] => F[A]): F[A]
}

object Tracer {

  /**
    * Create a `Tracer` that automatically logs an unhandled error and then
    * rethrows it
    */
  def reporting[F[_]: Sync](logger: Logger[F]): Tracer[F] =
    new Tracer[F] {
      override def run[A](f: Logger[F] => F[A]): F[A] =
        UUIDs.random[F].flatMap { trace =>
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
        UUIDs.random[F].flatMap { trace =>
          val tracer = logger.trace(trace)
          f(tracer).adaptErr {
            case throwable =>
              TracedFailure(trace, logger.prefix, logger.presets, throwable)
          }
        }
    }
}
