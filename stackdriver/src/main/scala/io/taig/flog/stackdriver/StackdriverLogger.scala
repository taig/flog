package io.taig.flog.stackdriver

import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{Option => _, _}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import io.taig.flog.util.Printer
import io.taig.flog.stackdriver.interal.Circe
import io.taig.flog.Event
import io.taig.flog.data.Event

import scala.jdk.CollectionConverters._

object StackdriverLogger {
  def apply[F[_]: Clock](
      logging: Logging,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Logger[F] = Logger { event =>
    F.delay(logging.write(List(entry(event, name, resource)).asJava))
  }

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]: Clock](name: String, resource: MonitoredResource)(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] = {
    val acquire = F.delay(LoggingOptions.getDefaultInstance.getService)
    val release = (logging: Logging) => F.delay(logging.close())
    Resource.make(acquire)(release).map { logging =>
      StackdriverLogger[F](logging, name, resource)
    }
  }

  def default[F[_]: Sync: Clock](
      name: String,
      tpe: String = "global"
  ): Resource[F, Logger[F]] =
    default(name, MonitoredResource.newBuilder(tpe).build())

  def entry(
      event: Event,
      name: String,
      resource: MonitoredResource
  ): LogEntry =
    LogEntry
      .newBuilder(payload(event))
      .setSeverity(severity(event))
      .setResource(resource)
      .setLogName(name)
      .setTimestamp(event.timestamp)
      .build()

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
