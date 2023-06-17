package io.taig.flog.http4s

import cats.effect.{Concurrent, Resource}
import io.circe.syntax.*
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import io.taig.flog.http4s.implicits.*
import org.http4s.client.Client

object LoggingClient {
  def apply[F[_]: Concurrent](logger: Logger[F])(client: Client[F]): Client[F] =
    create(logger.prepend(Scope.one("client")), client)

  private def create[F[_]: Concurrent](logger: Logger[F], client: Client[F]): Client[F] = Client[F] { request =>
    for
      _ <- Resource.eval(logger.info("Request", request.asJsonObject))
      response <- client.run(request)
      _ <- Resource.eval(logger.info("Response", response.asJsonObject))
    yield response
  }
}
