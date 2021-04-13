package org.slf4j.impl

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._

import cats.effect.Sync
import cats.effect.std.Dispatcher
import io.taig.flog.Logger
import io.taig.flog.data.{Level, Scope}
import org.slf4j.{ILoggerFactory, Logger => Slf4jLogger}

class FlogLoggerFactory[F[_]] extends ILoggerFactory {
  private var target: Logger[F] = null
  private var dispatcher: Dispatcher[F] = null
  private val loggers = new ConcurrentHashMap[String, Slf4jLogger]().asScala

  private def createLogger(name: String): Slf4jLogger = {
    val scope = Scope.from(name.split('.'))
    val log: (Level, String, Option[Throwable]) => Unit = { (level, message, throwable) =>
      if (target == null || dispatcher == null)
        System.err.println("Observed slf4j log message, but FlogLoggerFactory has not been initialized yet")
      else dispatcher.unsafeRunAndForget(target.apply(level, scope, message, throwable = throwable))
    }

    new FlogSlf4jLogger(log)
  }

  override def getLogger(name: String): Slf4jLogger = loggers.getOrElseUpdate(name, createLogger(name))
}

object FlogLoggerFactory {
  def initialize[F[_]](logger: Logger[F], dispatcher: Dispatcher[F])(implicit F: Sync[F]): F[Unit] = F.delay {
    val factory = StaticLoggerBinder.getSingleton.getLoggerFactory.asInstanceOf[FlogLoggerFactory[F]]
    factory.target = logger
    factory.dispatcher = dispatcher
  }
}
