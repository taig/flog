package io.taig.flog.stackdriver

import cats.effect.Sync
import cats.implicits._
import com.google.cloud.logging.Logging.WriteOption
import com.google.cloud.logging.Payload.StringPayload
import com.google.cloud.logging.{Option => _, _}
import io.taig.flog.internal.Helpers
import io.taig.flog.{Event, Level, Logger}

import scala.jdk.CollectionConverters._

final class StackdriverLogger[F[_]](
    logging: Logging,
    build: LogEntry.Builder => LogEntry.Builder,
    write: List[WriteOption]
)(implicit F: Sync[F])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] = {
    val entries = events.map { event =>
      val builder = LogEntry
        .newBuilder(payload(event).map(StringPayload.of).orNull)
        .setSeverity(severity(event))
        .setLogName(name(event))
        .setLabels(event.payload.value.asJava)
        .setTimestamp(event.timestamp.toEpochMilli)

      build(builder).build()
    }

    F.delay(logging.write(entries.asJava, write: _*))
  }

  def payload(event: Event): Option[String] =
    (Some(event.message.value).filter(_.nonEmpty), event.throwable) match {
      case (Some(message), Some(throwable)) =>
        Some(message + "\n" + Helpers.print(throwable))
      case (message @ Some(_), None) => message
      case (None, Some(throwable))   => Some(Helpers.print(throwable))
      case (None, None)              => None
    }

  def name(event: Event): String = event.scope.show.replace("/", "%2F")

  def severity(event: Event): Severity = event.level match {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Failure => Severity.CRITICAL
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}

object StackdriverLogger {
  def apply[F[_]: Sync](
      logging: Logging,
      build: LogEntry.Builder => LogEntry.Builder,
      write: List[WriteOption]
  ): Logger[F] =
    new StackdriverLogger[F](logging, build, write)

  def default[F[_]](implicit F: Sync[F]): F[Logger[F]] =
    F.delay(LoggingOptions.getDefaultInstance.getService)
      .map(StackdriverLogger[F](_, build = identity, write = List.empty))
}
