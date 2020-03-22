package io.taig.flog.slf4j

import cats.effect.Effect
import io.taig.flog.algebra.Logger
import org.slf4j.ILoggerFactory

object FlogSlf4jBinder {
  private var factory: Option[ILoggerFactory] = None

  final def initialize[F[_]](
      logger: Logger[F]
  )(implicit F: Effect[F]): F[Unit] =
    F.delay {
      factory = Some(new FlogLoggerFactory[F](logger))
    }

  def getFactory(): Option[ILoggerFactory] = factory
}
