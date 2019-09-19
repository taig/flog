package io.taig.flog

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.UUIDs

final class Tracer[F[_], A](logger: Logger[F], f: Logger[F] => F[A])(
    implicit F: Sync[F]
) {
  val release: F[A] = UUIDs.random[F].flatMap(release(_))

  def release(trace: UUID): F[A] =
    release(logger.tracer(trace))

  def release(logger: Logger[F]): F[A] =
    f(logger).handleErrorWith { throwable =>
      logger.failure(throwable = throwable.some) *> F.raiseError(throwable)
    }

  def evalMap[B](f: A => F[B]): Tracer[F, B] =
    Tracer(logger)(release(_).flatMap(f))
}

object Tracer {
  def apply[F[_]: Sync, A](
      logger: Logger[F]
  )(f: Logger[F] => F[A]): Tracer[F, A] =
    new Tracer[F, A](logger, f)
}
