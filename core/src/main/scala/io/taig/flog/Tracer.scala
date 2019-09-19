package io.taig.flog

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.UUIDs

abstract class Tracer[F[_]] { self =>
  def run[A](f: Logger[F] => F[A]): F[A]

  final def edit(f: Logger[F] => Logger[F]): Tracer[F] =
    new Tracer[F] {
      override def run[A](g: Logger[F] => F[A]): F[A] = self.run(f andThen g)
    }
}

object Tracer {
  def apply[F[_]: Sync](logger: Logger[F]): Tracer[F] = new Tracer[F] {
    override def run[A](f: Logger[F] => F[A]): F[A] =
      UUIDs.random[F].flatMap { trace =>
        val tracer = logger.tracer(trace)

        f(tracer).handleErrorWith { throwable =>
          tracer.failure(throwable = throwable.some) *>
            throwable.raiseError[F, A]
        }
      }
  }
}
