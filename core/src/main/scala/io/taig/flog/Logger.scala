package io.taig.flog

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.util.UUID

import cats._
import cats.effect.Sync
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.internal.Times

import scala.reflect._

abstract class Logger[F[_]](val prefix: Scope, val presets: JsonObject) {
  protected def write(event: => Event): F[Unit]

  final def apply(event: => Event): F[Unit] = write(build(event))

  private def build(event: Event): Event =
    event.copy(scope = prefix ++ event.scope)

  final def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Event(level, scope, message, payload, throwable))

  final def mapK[G[_]](f: F ~> G): Logger[G] =
    Logger(event => f(write(event)), prefix, presets)

  final def trace(uuid: UUID): Logger[F] =
    Logger.preset(JsonObject("trace" -> uuid.asJson), this)

  final def append(scope: Scope): Logger[F] = Logger.append(scope, this)

  final def append(segment: String): Logger[F] = append(Scope.Root / segment)

  /** Append simple name of `A` as `Scope` */
  final def append[A: ClassTag]: Logger[F] =
    append(classTag[A].runtimeClass.getSimpleName)

  final def scope[A: ClassTag]: Logger[F] =
    Logger(write, Scope.fromClassName[A], presets)

  final def debug(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Debug, scope, message, payload, throwable)

  final def error(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Error, scope, message, payload, throwable)

  final def info(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Info, scope, message, payload, throwable)

  final def warning(
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Level.Warning, scope, message, payload, throwable)
}

object Logger {
  def apply[F[_]](
      f: (=> Event) => F[Unit],
      prefix: Scope = Scope.Root,
      presets: JsonObject = JsonObject.empty
  ): Logger[F] =
    new Logger[F](prefix, presets) {
      override def write(event: => Event): F[Unit] = f(event)
    }

  def writer[F[_]](
      target: OutputStream,
      buffer: Int
  )(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))
      .map { writer =>
        val flush = F.delay(writer.flush())

        Logger { event =>
          Times.now[F].flatMap { timestamp =>
            F.delay(writer.write(Event.render(timestamp, event).show))
          } *> flush
        }
      }

  def stdOut[F[_]: Sync](buffer: Int): F[Logger[F]] = writer(System.out, buffer)

  def stdOut[F[_]: Sync]: F[Logger[F]] = stdOut[F](buffer = 1024)

  def broadcast[F[_]: Applicative](loggers: Logger[F]*): Logger[F] =
    Logger(event => loggers.toList.traverse_(_.apply(event)))

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = Logger(_ => F.unit)

  def scoped[F[_]](logger: Logger[F])(f: Scope => Scope): Logger[F] =
    Logger(logger.write, f(logger.prefix), logger.presets)

  /** Append `Scope` to the `Logger` */
  def append[F[_]](scope: Scope, logger: Logger[F]): Logger[F] =
    scoped(logger)(_ ++ scope)

  def preset[F[_]](payload: JsonObject, logger: Logger[F]): Logger[F] = {
    val update = JsonObject.fromMap(logger.presets.toMap ++ payload.toMap)
    Logger(logger.write, logger.prefix, update)
  }
}
