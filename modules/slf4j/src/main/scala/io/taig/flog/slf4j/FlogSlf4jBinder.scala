package io.taig.flog.slf4j

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource, Sync}
import io.taig.flog.Logger
import org.slf4j.impl.FlogLoggerFactory

object FlogSlf4jBinder {
  final def initialize[F[_]: Sync](logger: Logger[F], dispatcher: Dispatcher[F]): F[Unit] =
    FlogLoggerFactory.initialize(logger, dispatcher)
}
