package io.taig.flog.http4s

import cats.effect.{Concurrent, Resource}
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.http4s._
import org.http4s.client.Client

object LoggingClient {
  def apply[F[_]: Concurrent](logger: Logger[F])(client: Client[F]): Client[F] =
    create(Logger.prefix(Scope.Root / "client")(logger), client)

  private def create[F[_]: Concurrent](logger: Logger[F], client: Client[F]): Client[F] = Client[F] { request =>
    for {
      _ <- Resource.liftF(logger.info("Request", encode(request)))
      response <- client.run(request)
      _ <- Resource.liftF(logger.info("Response", encode(response)))
    } yield response
  }

  private def encode[F[_]](request: Request[F]) =
    JsonObject(
      "request" := Json.obj(
        "method" := request.method.renderString,
        "uri" := request.uri.renderString,
        "headers" := request.headers.toList.map(_.renderString)
      )
    )

  private def encode[F[_]](response: Response[F]) =
    JsonObject(
      "response" := Json.obj(
        "status" := response.status.renderString,
        "headers" := response.headers.toList.map(_.renderString)
      )
    )
}
