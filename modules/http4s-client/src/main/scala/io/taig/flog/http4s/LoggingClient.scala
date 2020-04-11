package io.taig.flog.http4s

import cats.effect.{Concurrent, Resource}
import cats.implicits._
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.http4s._
import org.http4s.client.Client

object LoggingClient {
  val RequestScope: Scope = Scope.Root / "client" / "request"

  val ResponseScope: Scope = Scope.Root / "client" / "response"

  def apply[F[_]: Concurrent](client: Client[F], logger: Logger[F]): Client[F] =
    response(request(client, logger), logger)

  def request[F[_]: Concurrent](client: Client[F], logger: Logger[F]): Client[F] = Client[F] { request =>
    Resource.liftF(logger.info(RequestScope, encode(request))) *> client.run(request)
  }

  def response[F[_]: Concurrent](client: Client[F], logger: Logger[F]): Client[F] = Client { request =>
    client.run(request).evalTap(response => logger.info(ResponseScope, encode(response)))
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
