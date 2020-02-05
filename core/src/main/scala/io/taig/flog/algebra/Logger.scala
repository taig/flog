package io.taig.flog.algebra

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import io.circe.JsonObject
import io.taig.flog.data.{Event, Level, Scope}
import io.taig.flog.util.Printer

import scala.concurrent.duration.MILLISECONDS

abstract class Logger[F[_]] {
  def log(event: Long => Event): F[Unit]

  final def apply(event: Long => Event): F[Unit] =
    log(timestamp => event(timestamp))

  final def mapK[G[_]](fk: F ~> G): Logger[G] = event => fk(log(event))

  def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(Event(_, level, scope, message, payload, throwable))

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
      write: Event => F[Unit]
  )(implicit clock: Clock[F]): Logger[F] =
    clock.realTime(MILLISECONDS).map(_).flatMap(write)

  // TODO should we close the target except for stdout and thus make this a resource?
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

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit
}
