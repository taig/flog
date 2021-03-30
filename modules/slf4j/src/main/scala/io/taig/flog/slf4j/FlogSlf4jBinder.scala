package io.taig.flog.slf4j

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource, Sync}
import io.taig.flog.Logger
import org.slf4j.ILoggerFactory

object FlogSlf4jBinder {
  private var factory: Option[ILoggerFactory] = None

  final def initialize[F[_]](logger: Logger[F], dispatcher: Dispatcher[F])(implicit F: Sync[F]): F[Unit] =
    F.delay { factory = Some(new FlogLoggerFactory[F](logger)(dispatcher)) }

  final def initialize[F[_]](logger: Logger[F])(implicit F: Async[F]): Resource[F, Unit] =
    Dispatcher[F].evalMap(initialize[F](logger, _))

  def getFactory(): Option[ILoggerFactory] = factory
}
