package io.taig.flog.stackdriver

import java.util.Collections

import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{Option => _, _}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.data.{Event, Level}
import io.taig.flog.stackdriver.interal.Circe
import io.taig.flog.util.Printer
import scala.jdk.CollectionConverters._

import io.taig.flog.Logger

object StackdriverLogger {
  def apply[F[_]: Clock](logging: Logging, resource: MonitoredResource)(implicit F: Sync[F]): Logger[F] =
    Logger { events =>
      val entries = events.map(entry(_, resource))

      F.delay(logging.write(entries.asJava))
        .handleErrorWith { throwable =>
          val entries = Collections.singleton(failureEntry(resource, throwable))
          F.delay(logging.write(entries))
        }
        .handleErrorWith { throwable =>
          F.delay(throwable.printStackTrace(System.err))
        }
    }

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]: Clock](resource: MonitoredResource)(implicit F: Sync[F]): Resource[F, Logger[F]] =
    Resource
      .fromAutoCloseable[F, Logging](F.delay(LoggingOptions.getDefaultInstance.getService))
      .map(StackdriverLogger[F](_, resource))

  def entry(event: Event, resource: MonitoredResource): LogEntry =
    LogEntry
      .newBuilder(payload(event))
      .setLogName(event.scope.segments.mkString("."))
      .setSeverity(severity(event))
      .setResource(resource)
      .setTimestamp(event.timestamp)
      .build()

  def failureEntry(resource: MonitoredResource, throwable: Throwable): LogEntry = {
    val payload = Map(
      "message" -> "Failed to submit events",
      "stacktrace" -> Printer.throwable(throwable)
    ).asJava

    LogEntry
      .newBuilder(JsonPayload.of(payload))
      .setSeverity(Severity.ERROR)
      .setResource(resource)
      .build()
  }

  def payload(event: Event): JsonPayload = {
    val json = JsonObject(
      "message" -> Option(event.message).filter(_.nonEmpty).asJson,
      "payload" -> event.payload.asJson,
      "stacktrace" -> event.throwable.map(Printer.throwable).asJson
    )

    JsonPayload.of(Circe.toJavaMap(json))
  }

  def severity(event: Event): Severity = event.level match {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}
