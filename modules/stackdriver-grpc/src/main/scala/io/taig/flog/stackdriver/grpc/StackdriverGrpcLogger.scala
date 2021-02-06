package io.taig.flog.stackdriver.grpc

import java.util
import java.util.{Collections, UUID}

import scala.jdk.CollectionConverters._

import cats.effect.{Clock, Resource, Sync}
import cats.syntax.all._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{LogEntry, Logging, LoggingOptions, Severity}
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level, Payload}
import io.taig.flog.syntax._
import io.taig.flog.util.StacktracePrinter

object StackdriverGrpcLogger {
  def apply[F[_]: Clock](name: String, logging: Logging, resource: MonitoredResource)(implicit F: Sync[F]): Logger[F] =
    Logger { events =>
      events
        .traverse(entry(name, _, resource))
        .flatMap(entries => F.delay(logging.write(entries.asJava)))
        .handleErrorWith { throwable =>
          failureEntry(name, resource, throwable)
            .map(Collections.singleton[LogEntry])
            .flatMap(entries => F.delay(logging.write(entries)))
        }
        .handleErrorWith(throwable => F.delay(throwable.printStackTrace(System.err)))
    }

  def fromOptions[F[_]: Clock](name: String, resource: MonitoredResource, options: LoggingOptions)(implicit
      F: Sync[F]
  ): Resource[F, Logger[F]] =
    Resource
      .fromAutoCloseable[F, Logging](F.delay(options.getService))
      .map(StackdriverGrpcLogger[F](name, _, resource))

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]: Clock](name: String, resource: MonitoredResource)(implicit F: Sync[F]): Resource[F, Logger[F]] =
    fromOptions(name, resource, LoggingOptions.getDefaultInstance)

  private def id[F[_]](implicit F: Sync[F]): F[String] = F.delay(UUID.randomUUID().show)

  private def entry[F[_]: Sync](name: String, event: Event, resource: MonitoredResource): F[LogEntry] =
    id[F].map { id =>
      LogEntry
        .newBuilder(payload(event))
        .setLogName((name +: event.scope.segments.toList).mkString("."))
        .setInsertId(id)
        .setSeverity(severity(event.level))
        .setResource(resource)
        .setTimestamp(event.timestamp)
        .build()
    }

  private def failureEntry[F[_]: Sync](name: String, resource: MonitoredResource, throwable: Throwable): F[LogEntry] =
    id[F].map { id =>
      // format: off
      val payload = util.Map.of[String, Object](
        "message", "Failed to submit events",
        "stacktrace", StacktracePrinter(throwable)
      )
      // format: on

      LogEntry
        .newBuilder(JsonPayload.of(payload))
        .setLogName(name)
        .setInsertId(id)
        .setSeverity(Severity.ERROR)
        .setResource(resource)
        .build()
    }

  private def payload(event: Event): JsonPayload = {
    val payload = Payload
      .of(
        "message" := Option(event.message).filter(_.nonEmpty),
        "payload" := event.payload,
        "stacktrace" := event.throwable.map(StacktracePrinter(_))
      )
      .toJavaMap

    JsonPayload.of(payload)
  }

  private val severity: Level => Severity = {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}
