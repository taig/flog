package io.taig.flog.stackdriver.grpc

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import com.github.slugify.Slugify
import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{LogEntry, Logging, LoggingOptions, Severity}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level}
import io.taig.flog.util.{JsonObjects, StacktracePrinter}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util
import java.util.{Arrays => JArrays, Collections, UUID}
import scala.jdk.CollectionConverters._

object StackdriverGrpcLogger {
  private val Scopes = JArrays.asList(
    "https://www.googleapis.com/auth/cloud-platform.read-only",
    "https://www.googleapis.com/auth/logging.write"
  )

  private val slugify = new Slugify().withLowerCase(false)

  def apply[F[_]](logging: Logging, name: String, resource: MonitoredResource)(implicit F: Sync[F]): Logger[F] =
    Logger { events =>
      events
        .traverse(entry(name, _, resource))
        .flatMap { entries =>
          F.blocking(logging.write(entries.asJava)).handleErrorWith { throwable =>
            failureEntry(name, resource, throwable, entries).flatMap { entry =>
              F.blocking(logging.write(Collections.singleton(entry)))
            }
          }
        }
        .handleErrorWith(throwable => F.delay(throwable.printStackTrace(System.err)))
    }

  def fromCredentials[F[_]](
      credentials: Credentials,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Resource[F, Logger[F]] = {
    val options = LoggingOptions.newBuilder().setCredentials(credentials).build()
    Resource.fromAutoCloseable(F.blocking(options.getService)).map(StackdriverGrpcLogger(_, name, resource))
  }

  def fromServiceAccount[F[_]](
      account: String,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Resource[F, Logger[F]] =
    Resource
      .eval {
        F.blocking {
          val input = new ByteArrayInputStream(account.getBytes(StandardCharsets.UTF_8))
          ServiceAccountCredentials.fromStream(input).createScoped(Scopes)
        }
      }
      .flatMap(fromCredentials(_, name, resource))

  private def id[F[_]](implicit F: Sync[F]): F[String] = F.delay(UUID.randomUUID().show)

  private def entry[F[_]: Sync](name: String, event: Event, resource: MonitoredResource): F[LogEntry] =
    id[F].map { id =>
      LogEntry
        .newBuilder(payload(event))
        .setLogName((name +: event.scope.segments.toList).map(slugify.slugify).mkString("."))
        .setInsertId(id)
        .setSeverity(severity(event.level))
        .setResource(resource)
        .setTimestamp(Instant.ofEpochMilli(event.timestamp))
        .build()
    }

  private def failureEntry[F[_]: Sync](
      name: String,
      resource: MonitoredResource,
      throwable: Throwable,
      entries: List[LogEntry]
  ): F[LogEntry] =
    id[F].map { id =>
      // format: off
      val payload = util.Map.of[String, Object](
        "message", "Failed to submit events",
        "stacktrace", StacktracePrinter(throwable),
        "entries", entries.map(_.toString).mkString("\n")
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
    val payload = JsonObjects.toJavaMap {
      JsonObject(
        "message" := Option(event.message).filter(_.nonEmpty),
        "payload" := event.payload,
        "stacktrace" := event.throwable.map(StacktracePrinter(_))
      )
    }

    JsonPayload.of(payload)
  }

  private val severity: Level => Severity = {
    case Level.Debug   => Severity.DEBUG
    case Level.Error   => Severity.ERROR
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.WARNING
  }
}
