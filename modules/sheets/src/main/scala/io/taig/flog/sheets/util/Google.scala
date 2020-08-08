package io.taig.flog.sheets.util

import java.io.InputStream
import java.util.{Arrays => JArrays}

import scala.jdk.CollectionConverters._

import cats.effect.{Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model.{AppendValuesResponse, ValueRange}
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.google.auth.Credentials
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials

object Google {
  def handle[F[_]](stream: F[InputStream])(implicit F: Sync[F]): Resource[F, InputStream] =
    Resource.make(stream)(resource => F.delay(resource.close()))

  def credentials[F[_]: Sync: ContextShift](blocker: Blocker, account: F[InputStream]): F[Credentials] =
    handle[F](account).use { input =>
      val scope = JArrays.asList(SheetsScopes.SPREADSHEETS)
      blocker.delay(ServiceAccountCredentials.fromStream(input).createScoped(scope))
    }

  def sheets[F[_]: ContextShift](blocker: Blocker, account: F[InputStream])(implicit F: Sync[F]): F[Sheets] =
    for {
      json <- F.delay(JacksonFactory.getDefaultInstance)
      transport <- F.delay(GoogleNetHttpTransport.newTrustedTransport())
      credentials <- credentials[F](blocker, account)
    } yield new Sheets.Builder(transport, json, new HttpCredentialsAdapter(credentials))
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
