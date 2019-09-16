package io.taig.flog.stackdriver

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Logging.WriteOption
import com.google.cloud.logging.Payload.StringPayload
import com.google.cloud.logging.{Option => _, _}
import io.taig.flog.internal.Helpers
import io.taig.flog.{Event, Level, Logger}

import scala.jdk.CollectionConverters._

final class StackdriverLogger[F[_]](
    logging: Logging,
    name: String,
    resource: MonitoredResource,
    build: LogEntry.Builder => LogEntry.Builder,
    write: List[WriteOption]
)(implicit F: Sync[F])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] = {
    val entries = events.map { event =>
      val stacktrace = event.throwable.map(Helpers.print)
      val payload = StringPayload.of(message(event, stacktrace))

      val builder = LogEntry
        .newBuilder(payload)
        .setSeverity(severity(event))
        .setResource(resource)
        .setLogName(name)
        .setTimestamp(event.timestamp.toEpochMilli)
        .setLabels(event.payload.value.asJava)
        .addLabel("scope", event.scope.show)

      build(builder).build()
    }

    F.delay(logging.write(entries.asJava, write: _*))
  }

  def message(event: Event, stacktrace: Option[String]): String =
    (Option(event.message.value).filter(_.nonEmpty), stacktrace) match {
      case (Some(message), Some(stacktrace)) => message + "\n" + stacktrace
      case (None, Some(stacktrace)) => stacktrace
      case (Some(message), None)    => message
      case (None, None)             => ""
    }

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
      name: String,
      resource: MonitoredResource,
      build: LogEntry.Builder => LogEntry.Builder,
      write: List[WriteOption]
  ): Logger[F] =
    new StackdriverLogger[F](logging, name, resource, build, write)

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]](name: String, resource: MonitoredResource)(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] = {
    val acquire = F.delay(LoggingOptions.getDefaultInstance.getService)
    val release = (logging: Logging) => F.delay(logging.close())
    Resource.make(acquire)(release).map { logging =>
      StackdriverLogger[F](logging, name, resource, identity, List.empty)
    }
  }
}
