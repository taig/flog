package io.taig.flog.slf4j

import cats.effect.Effect
import cats.effect.implicits._
import io.taig.flog.algebra.Logger
import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

abstract class FlogLoggerFactoryBinder[F[_]] extends LoggerFactoryBinder {
  implicit def effect: Effect[F]

  def logger: F[Logger[F]]

  var REQUESTED_API_VERSION: String = "1.7.30"

  final def getSingleton: this.type = this

  final override val getLoggerFactory: ILoggerFactory =
    new FlogLoggerFactory[F](logger.toIO.unsafeRunSync())

  final override val getLoggerFactoryClassStr: String = getClass.getName
}
