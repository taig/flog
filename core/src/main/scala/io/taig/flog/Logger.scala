package io.taig.flog

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.util.UUID

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.internal.{Circe, Printer}

import scala.concurrent.duration.MILLISECONDS
import scala.reflect._

/** Logger
  *
  * @param prefix A default `Scope` that is prepended to each `Event`'s `Scope`
  * @param presets A default payload that is merged with each `Event`'s payload
  */
abstract class Logger[F[_]](val prefix: Scope, val presets: JsonObject) {
  self =>

  /** Log the `Event` exactly as given (ignoring `prefix` and `presets`) to
    * the `Logger`'s effect
    */
  def log(event: Long => Event): F[Unit]

  /** Add this `Logger`'s `prefix` and `presets` data to the given `Event` */
  final def build(event: Event): Event =
    event.copy(
      scope = prefix ++ event.scope,
      payload = Circe.combine(presets, event.payload)
    )

  /** Write the `Event` after adding `prefix` and `presets` to the `Logger`'s
    * effect
    */
  final def apply(event: Long => Event): F[Unit] =
    log(timestamp => build(event(timestamp)))

  /** Write the `Event` after adding `prefix`, `presets` and the current
    * timestamp to the `Logger`'s effect
    */
  def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] = apply(Event(_, level, scope, message, payload, throwable))

  final def mapK[G[_]](f: F ~> G): Logger[G] =
    new Logger[G](prefix, presets) {
      override def log(event: Long => Event): G[Unit] = f(self.log(event))
    }

  final def trace(uuid: UUID): Logger[F] =
    Logger.preset(this)(JsonObject("trace" := uuid))

  final def append(scope: Scope): Logger[F] = Logger.append(this)(scope)

  final def append(segment: String): Logger[F] = append(Scope.Root / segment)

  /** Append simple name of `A` as `Scope` */
  final def append[A: ClassTag]: Logger[F] =
    append(classTag[A].runtimeClass.getSimpleName)

  final def scope[A: ClassTag]: Logger[F] =
    Logger.scoped(this)(_ => Scope.fromClassName[A])

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
  def apply[F[_]: FlatMap](
      write: Event => F[Unit],
      prefix: Scope = Scope.Root,
      presets: JsonObject = JsonObject.empty
  )(implicit clock: Clock[F]): Logger[F] = new Logger[F](prefix, presets) {
    override def log(event: Long => Event): F[Unit] =
      clock.realTime(MILLISECONDS).flatMap { timestamp =>
        write(event(timestamp))
      }
  }

  def writer[F[_]: Clock](
      target: OutputStream,
      buffer: Int
  )(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), buffer))
      .map { writer =>
        val flush = F.delay(writer.flush())

        Logger { event =>
          F.delay(writer.write(Printer.event(event).show)) *> flush
        }
      }

  def stdOut[F[_]: Sync: Clock](buffer: Int): F[Logger[F]] =
    writer(System.out, buffer)

  def stdOut[F[_]: Sync: Clock]: F[Logger[F]] = stdOut[F](buffer = 1024)

  def broadcast[F[_]: Monad: Clock](loggers: Logger[F]*): Logger[F] =
    if (loggers.isEmpty) noop[F]
    else Logger(event => loggers.toList.traverse_(_.log(_ => event)))

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] =
    new Logger[F](Scope.Root, JsonObject.empty) {
      override def log(event: Long => Event): F[Unit] = F.unit
    }

  def scoped[F[_]](logger: Logger[F])(f: Scope => Scope): Logger[F] =
    new Logger[F](f(logger.prefix), logger.presets) {
      override def log(event: Long => Event): F[Unit] = logger.log(event)
    }

  /** Append `Scope` to the `Logger` */
  def append[F[_]](logger: Logger[F])(scope: Scope): Logger[F] =
    scoped(logger)(_ ++ scope)

  def preset[F[_]](logger: Logger[F])(payload: JsonObject): Logger[F] =
    new Logger[F](logger.prefix, payload) {
      override def log(event: Long => Event): F[Unit] = logger.log(event)
    }
}
