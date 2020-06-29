package io.taig.flog.stackdriver

import java.util.{Collections, UUID}

import scala.jdk.CollectionConverters._

import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{Option => _, _}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level}
import io.taig.flog.stackdriver.interal.Circe
import io.taig.flog.util.Printer

object StackdriverLogger {
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
        .handleErrorWith { throwable =>
          F.delay(throwable.printStackTrace(System.err))
        }
    }

  def fromOptions[F[_]: Clock](name: String, resource: MonitoredResource, options: LoggingOptions)(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] =
    Resource
      .fromAutoCloseable[F, Logging](F.delay(options.getService))
      .map(StackdriverLogger[F](name, _, resource))

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]: Clock](name: String, resource: MonitoredResource)(implicit F: Sync[F]): Resource[F, Logger[F]] =
    fromOptions(name, resource, LoggingOptions.getDefaultInstance)

  private def id[F[_]](implicit F: Sync[F]): F[String] = F.delay(UUID.randomUUID().show)

  private def entry[F[_]: Sync](name: String, event: Event, resource: MonitoredResource): F[LogEntry] =
    id[F].map { id =>
      LogEntry
        .newBuilder(payload(event))
        .setLogName((name +: event.scope.segments).mkString("."))
        .setInsertId(id)
        .setSeverity(severity(event.level))
        .setResource(resource)
        .setTimestamp(event.timestamp)
        .build()
    }

  private def failureEntry[F[_]: Sync](name: String, resource: MonitoredResource, throwable: Throwable): F[LogEntry] =
    id[F].map { id =>
      val payload = Map(
        "message" -> "Failed to submit events",
        "stacktrace" -> Printer.throwable(throwable)
      ).asJava

      LogEntry
        .newBuilder(JsonPayload.of(payload))
        .setLogName(name)
        .setInsertId(id)
        .setSeverity(Severity.ERROR)
        .setResource(resource)
        .build()
    }

  private def payload(event: Event): JsonPayload = {
    val json = JsonObject(
      "message" -> Option(event.message).filter(_.nonEmpty).asJson,
      "payload" -> event.payload.asJson,
      "stacktrace" -> event.throwable.map(Printer.throwable).asJson
    )

    JsonPayload.of(Circe.toJavaMap(json))
  }

  private val severity: Level => Severity = {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}
