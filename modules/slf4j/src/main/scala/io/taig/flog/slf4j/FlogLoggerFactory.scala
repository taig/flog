package io.taig.flog.slf4j

import cats.effect.std.Dispatcher
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.slf4j.{ILoggerFactory, Logger => Slf4jLogger}

final class FlogLoggerFactory[F[_]](logger: Logger[F])(dispatcher: Dispatcher[F]) extends ILoggerFactory {
  override def getLogger(name: String): Slf4jLogger = {
    val scope = Scope.from(name.split('.'))
    new FlogSlf4jLogger[F](logger.prepend(scope))(dispatcher)
  }
}
