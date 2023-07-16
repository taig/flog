package io.taig.flog.log4cats

import cats.Applicative
import cats.syntax.all.*
import io.circe.JsonObject
import io.circe.syntax.*
import io.taig.flog.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger

final class Log4CatsLogger[F[_]](logger: Logger[F])(using F: Applicative[F]) extends SelfAwareStructuredLogger[F]:
  // TODO expose this info from `Logger`
  override def isTraceEnabled: F[Boolean] = F.pure(true)
  override def isDebugEnabled: F[Boolean] = F.pure(true)
  override def isInfoEnabled: F[Boolean] = F.pure(true)
  override def isWarnEnabled: F[Boolean] = F.pure(true)
  override def isErrorEnabled: F[Boolean] = F.pure(true)

  def toPayload(ctx: Map[String, String]): JsonObject = JsonObject.fromMap(ctx.fmap(_.asJson))

  override def debug(ctx: Map[String, String])(msg: => String): F[Unit] = logger.debug(msg, toPayload(ctx))
  override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    logger.debug(msg, toPayload(ctx), t)
  override def debug(msg: => String): F[Unit] = logger.debug(msg)
  override def debug(t: Throwable)(msg: => String): F[Unit] = logger.debug(msg, t)
  override def error(ctx: Map[String, String])(msg: => String): F[Unit] = logger.error(msg, toPayload(ctx))
  override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    logger.error(msg, toPayload(ctx), t)
  override def error(msg: => String): F[Unit] = logger.error(msg)
  override def error(t: Throwable)(msg: => String): F[Unit] = logger.error(msg, t)
  override def info(ctx: Map[String, String])(msg: => String): F[Unit] = logger.info(msg, toPayload(ctx))
  override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    logger.info(msg, toPayload(ctx), t)
  override def info(msg: => String): F[Unit] = logger.info(msg)
  override def info(t: Throwable)(msg: => String): F[Unit] = logger.info(msg, t)
  override def trace(ctx: Map[String, String])(msg: => String): F[Unit] = logger.debug(msg, toPayload(ctx))
  override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    logger.debug(msg, toPayload(ctx), t)
  override def trace(msg: => String): F[Unit] = logger.debug(msg)
  override def trace(t: Throwable)(msg: => String): F[Unit] = logger.debug(msg, t)
  override def warn(ctx: Map[String, String])(msg: => String): F[Unit] = logger.warning(msg, toPayload(ctx))
  override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
    logger.warning(msg, toPayload(ctx), t)
  override def warn(msg: => String): F[Unit] = logger.warning(msg)
  override def warn(t: Throwable)(msg: => String): F[Unit] = logger.warning(msg, t)

object Log4CatsLogger:
  def apply[F[_]: Applicative](logger: Logger[F]): SelfAwareStructuredLogger[F] = new Log4CatsLogger[F](logger)
