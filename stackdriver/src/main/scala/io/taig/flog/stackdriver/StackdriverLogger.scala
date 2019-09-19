package io.taig.flog.stackdriver

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{Option => _, _}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.internal.Shows._
import io.taig.flog.stackdriver.interal.Circe
import io.taig.flog.{Event, Level, Logger, SyncLogger}

import scala.jdk.CollectionConverters._

object StackdriverLogger {
  def apply[F[_]](
      logging: Logging,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Logger[F] = SyncLogger { event =>
    F.delay(logging.write(List(entry(event, name, resource)).asJava))
  }

  // https://cloud.google.com/logging/docs/api/v2/resource-list
  def default[F[_]](name: String, resource: MonitoredResource)(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] = {
    val acquire = F.delay(LoggingOptions.getDefaultInstance.getService)
    val release = (logging: Logging) => F.delay(logging.close())
    Resource.make(acquire)(release).map { logging =>
      StackdriverLogger[F](logging, name, resource)
    }
  }

  def default[F[_]: Sync](
      name: String,
      tpe: String = "global"
  ): Resource[F, Logger[F]] =
    default(name, MonitoredResource.newBuilder(tpe).build())

  def entry(event: Event, name: String, resource: MonitoredResource): LogEntry =
    LogEntry
      .newBuilder(payload(event))
      .setSeverity(severity(event))
      .setResource(resource)
      .setLogName(name)
      .setTimestamp(event.timestamp.toEpochMilli)
      .build()

  def payload(event: Event): JsonPayload = {
    val json = JsonObject(
      "scope" -> event.scope.show.asJson,
      "message" -> Option(event.message.value).filter(_.nonEmpty).asJson,
      "payload" -> event.payload.value.asJson,
      "stacktrace" -> event.throwable.map(_.show).asJson
    )

    JsonPayload.of(Circe.toJavaMap(json))
  }

  def severity(event: Event): Severity = event.level match {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Failure => Severity.CRITICAL
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}
