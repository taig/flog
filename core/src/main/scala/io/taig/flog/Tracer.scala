package io.taig.flog

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.UUIDs

abstract class Tracer[F[_]] { self =>
  def run[A](f: Logger[F] => F[A]): F[A]
}

object Tracer {
  def apply[F[+_]: Sync, B](
      logger: Logger[F]
  )(f: (Logger[F], UUID, Throwable) => F[Nothing]): Tracer[F] = new Tracer[F] {
    override def run[A](g: Logger[F] => F[A]): F[A] =
      UUIDs.random[F].flatMap { trace =>
        val tracer = logger.tracer(trace)
        g(tracer).handleErrorWith(f(tracer, trace, _))
      }
  }

  def reporting[F[+_]: Sync](logger: Logger[F]): Tracer[F] =
    Tracer(logger) { (tracer, _, throwable) =>
      tracer.failure(throwable = throwable.some) *> throwable
        .raiseError[F, Nothing]
    }

  def adapting[F[+_]: Sync](logger: Logger[F]): Tracer[F] =
    Tracer(logger) { (tracer, trace, throwable) =>
      TracedFailure(tracer, trace, throwable).raiseError[F, Nothing]
    }
}
