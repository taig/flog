package io.taig.flog.sheets

import java.io.{FileNotFoundException, InputStream}
import java.util.Collections

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4._
import com.google.api.services.sheets.v4.model._

import scala.jdk.CollectionConverters._

object SheetsHelpers {
  def jackson[F[_]](implicit F: Sync[F]): F[JacksonFactory] =
    F.delay(JacksonFactory.getDefaultInstance)

  def transport[F[_]](implicit F: Sync[F]): F[NetHttpTransport] =
    F.delay(GoogleNetHttpTransport.newTrustedTransport())

  def resource[F[_]](name: String)(implicit F: Sync[F]): F[InputStream] =
    F.delay(Option(getClass.getResourceAsStream(name))).flatMap {
      case Some(resource) => resource.pure[F]
      case None =>
        val message = s"Resource not found: '$name'"
        new FileNotFoundException(message).raiseError[F, InputStream]
    }

  def handle[F[_]](
      stream: F[InputStream]
  )(implicit F: Sync[F]): Resource[F, InputStream] =
    Resource.make(stream)(resource => F.delay(resource.close()))

  def credential[F[_]](
      credentials: F[InputStream],
      json: JacksonFactory,
      transport: NetHttpTransport
  )(implicit F: Sync[F]): F[GoogleCredential] =
    handle[F](credentials).use { credentials =>
      val scope = Collections.singletonList(SheetsScopes.SPREADSHEETS)
      F.delay(GoogleCredential.fromStream(credentials, transport, json))
        .flatMap(credential => F.delay(credential.createScoped(scope)))
    }

  def sheets[F[_]: Sync](credentials: F[InputStream]): F[Sheets] =
    for {
      json <- jackson[F]
      transport <- transport[F]
      credential <- credential[F](credentials, json, transport)
    } yield new Sheets.Builder(transport, json, credential)
      .setApplicationName("flog")
      .build()

  def append[F[_]](sheets: Sheets, id: String, range: String)(
      body: List[List[AnyRef]]
  )(implicit F: Sync[F]): F[AppendValuesResponse] =
    for {
      body <- F.delay(new ValueRange().setValues(body.map(_.asJava).asJava))
      append = sheets.spreadsheets().values().append(id, range, body)
      _ <- F.delay(append.setValueInputOption("USER_ENTERED"))
      response <- F.delay(append.execute())
    } yield response
}
