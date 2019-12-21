package io.taig.flog.slf4j

import cats.effect.Sync
import cats.implicits._
import io.circe.JsonObject
import io.taig.flog.{Event, Level, Logger, Scope}
import org.slf4j.{Logger => JLogger, LoggerFactory => JLoggerFactory}

object Slf4jLogger {
  def apply[F[_]](implicit F: Sync[F]): Logger[F] =
    new Logger[F](Scope.Root, JsonObject.empty) {
      override def log(f: Long => Event): F[Unit] = {
        // slf4j ignores our timestamp anyway and provides its own,
        // so we use an arbitrary value here
        val event = f(0L)

        // TODO payload to marker
        // TODO logger cache (?)
        event.level match {
          case Level.Debug =>
            logger[F](event.scope).flatMap { logger =>
              F.delay(logger.debug(event.message, event.throwable.orNull))
            }
          case Level.Error =>
            logger[F](event.scope).flatMap { logger =>
              F.delay(logger.error(event.message, event.throwable.orNull))
            }
          case Level.Info =>
            logger[F](event.scope).flatMap { logger =>
              F.delay(logger.info(event.message, event.throwable.orNull))
            }
          case Level.Warning =>
            logger[F](event.scope).flatMap { logger =>
              F.delay(logger.warn(event.message, event.throwable.orNull))
            }
        }
      }
    }

  private def logger[F[_]](scope: Scope)(implicit F: Sync[F]): F[JLogger] =
    F.delay(JLoggerFactory.getLogger(scope.segments.mkString(".")))
}
