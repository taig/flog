package io.taig.flog.slf4j

import cats.effect.Effect
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.slf4j.{ILoggerFactory, Logger => Slf4jLogger}

final class FlogLoggerFactory[F[_]: Effect](logger: Logger[F]) extends ILoggerFactory {
  override def getLogger(name: String): Slf4jLogger = {
    val scope = Scope(name.split('.').toList)
    new FlogSlf4jLogger[F](Logger.prefix(scope)(logger))
  }
}
