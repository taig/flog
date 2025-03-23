package org.slf4j.impl

import cats.effect.Sync
import cats.effect.std.Dispatcher
import io.circe.JsonObject
import io.taig.flog.Logger
import io.taig.flog.data.Event
import io.taig.flog.data.Level
import io.taig.flog.data.Scope
import io.taig.flog.util.EventPrinter
import org.slf4j.ILoggerFactory
import org.slf4j.Logger as Slf4jLogger

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

final class FlogLoggerFactory[F[_]] extends ILoggerFactory:
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var target: Logger[F] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var dispatcher: Dispatcher[F] = null
  private val loggers = new ConcurrentHashMap[String, Slf4jLogger]().asScala

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.throw"))
  private def createLogger(name: String): Slf4jLogger =
    val scope = Scope.from(name.split('.'))
    val log: (Level, String, Option[Throwable]) => Unit = { (level, message, throwable) =>
      if target == null || dispatcher == null then {
        val details = EventPrinter(
          Event(System.currentTimeMillis(), level, scope, message, JsonObject.empty, throwable)
        )
        System.err.print(s"Observed slf4j log message, but FlogLoggerFactory has not been initialized yet:\n$details")
      } else {
        try dispatcher.unsafeRunAndForget(target.apply(level, scope, message, throwable = throwable))
        catch {
          case exception: IllegalStateException if exception.getMessage == "dispatcher already shutdown" => ()
          case throwable: Throwable => throw throwable
        }
      }
    }

    new FlogSlf4jLogger(log)

  override def getLogger(name: String): Slf4jLogger = loggers.getOrElseUpdate(name, createLogger(name))

object FlogLoggerFactory:
  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def initialize[F[_]](logger: Logger[F], dispatcher: Dispatcher[F])(implicit F: Sync[F]): F[Unit] = F.delay:
    val factory = StaticLoggerBinder.getSingleton.getLoggerFactory.asInstanceOf[FlogLoggerFactory[F]]
    factory.target = logger
    factory.dispatcher = dispatcher
