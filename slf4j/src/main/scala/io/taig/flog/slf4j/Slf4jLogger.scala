package io.taig.flog.slf4j

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.{Event, Level, Logger}
import org.slf4j.{Logger => JLogger, LoggerFactory => JLoggerFactory}

object Slf4jLogger {
  def apply[F[_]](implicit F: Sync[F]): Logger[F] = Logger { event =>
    // TODO payload to marker
    event.level match {
      case Level.Debug =>
        logger[F](event).flatMap(
          logger => F.delay(logger.debug(event.message, event.throwable.orNull))
        )
      case Level.Error =>
        logger[F](event).flatMap(
          logger => F.delay(logger.error(event.message, event.throwable.orNull))
        )
      case Level.Info =>
        logger[F](event).flatMap(
          logger => F.delay(logger.info(event.message, event.throwable.orNull))
        )
      case Level.Warning =>
        logger[F](event).flatMap(
          logger => F.delay(logger.warn(event.message, event.throwable.orNull))
        )
    }
  }

  private def logger[F[_]](event: Event)(implicit F: Sync[F]): F[JLogger] =
    F.delay(JLoggerFactory.getLogger(event.scope.segments.mkString(".")))
}
