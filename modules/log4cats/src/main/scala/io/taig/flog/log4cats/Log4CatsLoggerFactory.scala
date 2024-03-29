package io.taig.flog.log4cats

import cats.Applicative
import cats.syntax.all.*
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

final class Log4CatsLoggerFactory[F[_]: Applicative](logger: Logger[F]) extends LoggerFactory[F]:
  override def getLoggerFromName(name: String): SelfAwareStructuredLogger[F] =
    Log4CatsLogger(logger.append(Scope.fromName(name)))
  override def fromName(name: String): F[SelfAwareStructuredLogger[F]] =
    getLoggerFromName(name).pure[F]

object Log4CatsLoggerFactory:
  def apply[F[_]: Applicative](logger: Logger[F]): LoggerFactory[F] = new Log4CatsLoggerFactory[F](logger)
