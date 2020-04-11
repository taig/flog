package io.taig.flog.stackdriver

import java.util

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
  def apply[F[_]: Clock](logging: Logging, name: String, resource: MonitoredResource)(implicit F: Sync[F]): Logger[F] =
    Logger { events =>
      val entries = events.map(entry(_, name, resource))

      F.delay(logging.write(entries.asJava))
        .handleErrorWith { throwable =>
          val entries = util.Arrays.asList(failureEntry(name, resource, throwable))
          F.delay(logging.write(entries))
        }
        .handleErrorWith { throwable =>
          F.delay(throwable.printStackTrace(System.err))
        }
    }

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]: Clock](name: String, resource: MonitoredResource)(implicit F: Sync[F]): Resource[F, Logger[F]] =
    Resource.fromAutoCloseable(F.delay(LoggingOptions.getDefaultInstance.getService)).map { logging =>
      StackdriverLogger[F](logging, name, resource)
    }

  def entry(event: Event, name: String, resource: MonitoredResource): LogEntry =
    LogEntry
      .newBuilder(payload(event))
      .setSeverity(severity(event))
      .setResource(resource)
      .setLogName(name)
      .setTimestamp(event.timestamp)
      .build()

  def failureEntry(name: String, resource: MonitoredResource, throwable: Throwable): LogEntry = {
    val payload = Map(
      "message" -> "Failed to submit events",
      "stacktrace" -> Printer.throwable(throwable)
    ).asJava

    LogEntry
      .newBuilder(JsonPayload.of(payload))
      .setSeverity(Severity.ERROR)
      .setResource(resource)
      .setLogName(name)
      .build()
  }

  def payload(event: Event): JsonPayload = {
    val json = JsonObject(
      "scope" -> event.scope.show.asJson,
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
