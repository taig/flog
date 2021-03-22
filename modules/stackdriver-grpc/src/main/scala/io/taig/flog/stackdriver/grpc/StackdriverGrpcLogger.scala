package io.taig.flog.stackdriver.grpc

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util
import java.util.{Collections, UUID, Arrays => JArrays}

import scala.jdk.CollectionConverters._

import cats.effect.{Blocker, Clock, ContextShift, Resource, Sync}
import cats.syntax.all._
import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.MonitoredResource
import com.google.cloud.logging.Payload.JsonPayload
import com.google.cloud.logging.{LogEntry, Logging, LoggingOptions, Severity}
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level, Payload}
import io.taig.flog.syntax._
import io.taig.flog.util.StacktracePrinter

object StackdriverGrpcLogger {
  private val Scopes = JArrays.asList(
    "https://www.googleapis.com/auth/cloud-platform.read-only",
    "https://www.googleapis.com/auth/logging.write"
  )

  def apply[F[_]: ContextShift: Clock](blocker: Blocker, logging: Logging, name: String, resource: MonitoredResource)(
      implicit F: Sync[F]
  ): Logger[F] =
    Logger { events =>
      events
        .traverse(entry(name, _, resource))
        .flatMap(entries => blocker.delay(logging.write(entries.asJava)))
        .handleErrorWith { throwable =>
          failureEntry(name, resource, throwable).flatMap { entry =>
            blocker.delay(logging.write(Collections.singleton(entry)))
          }
        }
        .handleErrorWith(throwable => F.delay(throwable.printStackTrace(System.err)))
    }

  def fromCredentials[F[_]: ContextShift: Clock](
      blocker: Blocker,
      credentials: Credentials,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Resource[F, Logger[F]] = {
    val options = LoggingOptions.newBuilder().setCredentials(credentials).build()
    Resource
      .fromAutoCloseableBlocking(blocker)(F.delay(options.getService))
      .map(StackdriverGrpcLogger(blocker, _, name, resource))
  }

  def fromServiceAccount[F[_]: Sync: ContextShift: Clock](
      blocker: Blocker,
      account: String,
      name: String,
      resource: MonitoredResource
  ): Resource[F, Logger[F]] =
    Resource
      .eval {
        blocker.delay {
          val input = new ByteArrayInputStream(account.getBytes(StandardCharsets.UTF_8))
          ServiceAccountCredentials.fromStream(input).createScoped(Scopes)
        }
      }
      .flatMap(fromCredentials(blocker, _, name, resource))

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
