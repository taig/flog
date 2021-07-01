package io.taig.flog.stackdriver.http

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import com.github.slugify.Slugify
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.logging.v2.model.{LogEntry, MonitoredResource, WriteLogEntriesRequest}
import com.google.api.services.logging.v2.{Logging, LoggingScopes}
import com.google.auth.Credentials
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials
import io.taig.flog.Logger
import io.taig.flog.data.{Event, Level, Payload, Scope}
import io.taig.flog.syntax._
import io.taig.flog.util.StacktracePrinter

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.{UUID, Arrays => JArrays, Map => JMap}
import scala.jdk.CollectionConverters._

object StackdriverHttpLogger {
  private val Scopes = JArrays.asList(LoggingScopes.CLOUD_PLATFORM_READ_ONLY, LoggingScopes.LOGGING_WRITE)

  private val slugify = new Slugify().withLowerCase(false)

  def apply[F[_]](
      logging: Logging#Entries,
      project: String,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Logger[F] =
    Logger { events =>
      events
        .traverse(entry(project, name, _, resource))
        .flatMap { entries =>
          val request = new WriteLogEntriesRequest().setEntries(entries.asJava)
          F.delay(logging.write(request).execute()).void.handleErrorWith { throwable =>
            failureEntry(project, name, resource, throwable, entries)
              .flatMap { entry =>
                val request = new WriteLogEntriesRequest().setEntries(JArrays.asList(entry))
                F.delay(logging.write(request).execute()).void
              }
          }
        }
        .handleErrorWith(throwable => F.delay(throwable.printStackTrace(System.err)))
    }

  def fromCredentials[F[_]](credentials: Credentials, project: String, name: String, resource: MonitoredResource)(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] =
    Resource
      .make(F.delay(GoogleNetHttpTransport.newTrustedTransport()))(transport => F.delay(transport.shutdown()))
      .evalMap { transport =>
        F.delay {
          val logging = new Logging.Builder(
            transport,
            GsonFactory.getDefaultInstance,
            new HttpCredentialsAdapter(credentials)
          ).setApplicationName(project).build()

          StackdriverHttpLogger[F](logging.entries(), project, name, resource)
        }
      }

  def fromServiceAccount[F[_]](
      account: String,
      name: String,
      resource: MonitoredResource
  )(implicit F: Sync[F]): Resource[F, Logger[F]] =
    Resource
      .eval {
        F.delay {
          val input = new ByteArrayInputStream(account.getBytes(StandardCharsets.UTF_8))
          ServiceAccountCredentials.fromStream(input)
        }
      }
      .flatMap(account => fromCredentials(account.createScoped(Scopes), account.getProjectId, name, resource))

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
      throwable: Throwable,
      entries: List[LogEntry]
  ): F[LogEntry] =
    id[F].map { id =>
      // format: off
      val payload = JMap.of[String, Object](
        "message", "Failed to submit events",
        "stacktrace", StacktracePrinter(throwable),
        "entries", entries.map(_.toPrettyString).mkString("\n")
      )
      // format: on

      new LogEntry()
        .setJsonPayload(payload)
        .setLogName(logName(project, name, Scope.Root))
        .setInsertId(id)
        .setSeverity(severity(Level.Error))
        .setResource(resource)
    }

  private def logName(project: String, name: String, scope: Scope): String =
    s"projects/$project/logs/" + (name +: scope.segments.toList).map(slugify.slugify).mkString(".")

  private def payload(event: Event): JMap[String, Object] =
    Payload
      .of(
        "message" := Option(event.message).filter(_.nonEmpty),
        "payload" := event.payload,
        "stacktrace" := event.throwable.map(StacktracePrinter(_))
      )
      .toJavaMap

  private val severity: Level => String = {
    case Level.Debug   => "DEBUG"
    case Level.Error   => "ERROR"
    case Level.Info    => "INFO"
    case Level.Warning => "WARNING"
  }
}
