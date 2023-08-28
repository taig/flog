package io.taig.flog.slf4j2

import cats.effect.Sync
import cats.effect.std.Dispatcher
import io.taig.flog.Logger
import org.slf4j.LoggerFactory as Slf4jLoggerFactory

object FlogSlf4j2Binder:
  final def initialize[F[_]](logger: Logger[F], dispatcher: Dispatcher[F])(using F: Sync[F]): F[Unit] = F.delay:
    val factory = Slf4jLoggerFactory.getILoggerFactory.asInstanceOf[LoggerFactory]
    factory.attacheRuntime(new FlogSlf4j2Runtime[F](logger, dispatcher))
