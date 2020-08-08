package io.taig.flog.stackdriver.http

import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.{UUID, Arrays => JArrays, Map => JMap}

import scala.jdk.CollectionConverters._
import cats.effect.{Blocker, Clock, ContextShift, Resource, Sync}
import cats.implicits._
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.logging.v2.model.{LogEntry, MonitoredResource, WriteLogEntriesRequest}
import com.google.api.services.logging.v2.{Logging, LoggingScopes}
import com.google.auth.Credentials
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level, Scope}
import io.taig.flog.util.{Circe, Printer}

object StackdriverHttpLogger {
  def apply[F[_]: ContextShift: Clock](
      blocker: Blocker,
      logging: Logging#Entries,
      project: String,
      name: String,
      resource: MonitoredResource
  )(
      implicit F: Sync[F]
  ): Logger[F] =
    Logger { events =>
      events
        .traverse(entry(project, name, _, resource))
        .flatMap { entries =>
          val request = new WriteLogEntriesRequest().setEntries(entries.asJava)
          blocker.delay(logging.write(request).execute()).void
        }
        .handleErrorWith { throwable =>
          failureEntry(project, name, resource, throwable)
            .flatMap { entry =>
              val request = new WriteLogEntriesRequest().setEntries(JArrays.asList(entry))
              blocker.delay(logging.write(request).execute()).void
            }
        }
        .handleErrorWith(throwable => F.delay(throwable.printStackTrace(System.err)))
    }

  def fromCredentials[F[_]: Sync: ContextShift: Clock](
      blocker: Blocker,
      credentials: Credentials,
      project: String,
      name: String,
      resource: MonitoredResource
  ): Resource[F, Logger[F]] =
    Resource
      .make(blocker.delay(GoogleNetHttpTransport.newTrustedTransport()))(transport =>
        blocker.delay(transport.shutdown())
      )
      .evalMap { transport =>
        blocker.delay {
          val logging = new Logging.Builder(
            transport,
            JacksonFactory.getDefaultInstance,
            new HttpCredentialsAdapter(credentials)
          ).setApplicationName(project).build()

          StackdriverHttpLogger[F](blocker, logging.entries(), project, name, resource)
        }
      }

  def fromServiceAccount[F[_]: Sync: ContextShift: Clock](
      blocker: Blocker,
      account: InputStream,
      project: String,
      name: String,
      resource: MonitoredResource
  ): Resource[F, Logger[F]] =
    Resource
      .liftF {
        blocker
          .delay {
            val scopes = JArrays.asList(LoggingScopes.CLOUD_PLATFORM_READ_ONLY, LoggingScopes.LOGGING_WRITE)
            ServiceAccountCredentials.fromStream(account).createScoped(scopes)
          }
      }
      .flatMap(fromCredentials(blocker, _, project, name, resource))

  private def id[F[_]](implicit F: Sync[F]): F[String] = F.delay(UUID.randomUUID().show)

  private def entry[F[_]: Sync](project: String, name: String, event: Event, resource: MonitoredResource): F[LogEntry] =
    id[F].map { id =>
      new LogEntry()
        .setJsonPayload(payload(event))
        .setLogName(logName(project, name, event.scope))
        .setInsertId(id)
        .setSeverity(severity(event.level))
        .setResource(resource)
        .setTimestamp(Instant.ofEpochMilli(event.timestamp).toString)
    }

  private def failureEntry[F[_]: Sync](
      project: String,
      name: String,
      resource: MonitoredResource,
      throwable: Throwable
  ): F[LogEntry] =
    id[F].map { id =>
      // format: off
      val payload = JMap.of[String, Object](
        "message", "Failed to submit events",
        "stacktrace", Printer.throwable(throwable)
      )
      // format: on

      new LogEntry()
        .setJsonPayload(payload)
        .setLogName(logName(project, name, Scope.Root))
        .setInsertId(id)
        .setSeverity(severity(Level.Error))
        .setResource(resource)
    }

  def logName(project: String, name: String, scope: Scope): String =
    s"projects/$project/logs/" + URLEncoder
      .encode((name +: scope.segments).mkString("."), StandardCharsets.UTF_8)

  private def payload(event: Event): JMap[String, Object] = {
    val json = JsonObject(
      "message" -> Option(event.message).filter(_.nonEmpty).asJson,
      "payload" -> event.payload.asJson,
      "stacktrace" -> event.throwable.map(Printer.throwable).asJson
    )

    Circe.toJavaMap(json)
  }

  private val severity: Level => String = {
    case Level.Debug   => "DEBUG"
    case Level.Error   => "ERROR"
    case Level.Info    => "INFO"
    case Level.Warning => "WARNING"
  }
}
