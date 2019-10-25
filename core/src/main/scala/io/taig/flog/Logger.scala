package io.taig.flog

import java.io.{BufferedWriter, OutputStream, OutputStreamWriter}
import java.time.Instant
import java.util.UUID

import cats._
import cats.effect.Sync
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.internal.Times

abstract class Logger[F[_]] {
  def apply(event: Instant => Event): F[Unit]

  final def trace(id: UUID): Logger[F] =
    Logger.preset(JsonObject("trace" -> id.asJson), this)

  final def prefix(scope: Scope): Logger[F] = Logger.prefix(scope, this)

  final def prefix(segment: String): Logger[F] = prefix(Scope.Root / segment)

  final def apply(
      level: Level,
      scope: Scope = Scope.Root,
      message: Eval[String] = Eval.now(""),
      payload: Eval[JsonObject] = Eval.now(JsonObject.empty),
      throwable: Option[Throwable] = None
  ): F[Unit] = apply { timestamp =>
    Event(
      level,
      scope,
      timestamp,
      message,
      payload,
      throwable
    )
  }

  final def debug(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Debug,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def error(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Error,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def info(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Info,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def failure(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Failure,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )

  final def warning(
      scope: Scope = Scope.Root,
      message: => String = "",
      payload: => JsonObject = JsonObject.empty,
      throwable: Option[Throwable] = None
  ): F[Unit] =
    apply(
      Level.Warning,
      scope,
      Eval.later(message),
      Eval.later(payload),
      throwable
    )
}

object Logger {
  def apply[F[_]: Sync](f: Event => F[Unit]): Logger[F] =
    event => Times.now[F].map(event) >>= f

  def writer[F[_]](
      target: OutputStream
  )(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), 1024))
      .map { writer =>
        val flush = F.delay(writer.flush())
        Logger(event => F.delay(writer.write(event.show)) *> flush)
      }

  def stdOut[F[_]: Sync]: F[Logger[F]] = writer(System.out)

  def broadcast[F[_]: Applicative](loggers: Logger[F]*): Logger[F] =
    event => loggers.toList.traverse_(_.apply(event))

  def noop[F[_]](implicit F: Applicative[F]): Logger[F] = _ => F.unit

  def prefix[F[_]](scope: Scope, logger: Logger[F]): Logger[F] =
    event =>
      logger { timestamp =>
        val previous = event(timestamp)
        previous.copy(scope = scope ++ previous.scope)
      }

  def preset[F[_]](payload: JsonObject, logger: Logger[F]): Logger[F] =
    event =>
      logger { timestamp =>
        val previous = event(timestamp)
        val preset = payload.toMap
        val update = previous.payload.map { payload =>
          JsonObject.fromMap(preset ++ payload.toMap)
        }
        previous.copy(payload = update)
      }

}
