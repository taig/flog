package io.taig.flog.http4s

import cats.effect.{Concurrent, Resource}
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import io.taig.flog.http4s.implicits._
import io.taig.flog.syntax._
import org.http4s.client.Client

object LoggingClient {
  def apply[F[_]: Concurrent](logger: Logger[F])(client: Client[F]): Client[F] =
    create(logger.prepend(Scope.one("client")), client)

  private def create[F[_]: Concurrent](logger: Logger[F], client: Client[F]): Client[F] = Client[F] { request =>
    for {
      _ <- Resource.liftF(logger.info("Request", request.asObject))
      response <- client.run(request)
      _ <- Resource.liftF(logger.info("Response", response.asObject))
    } yield response
  }
}
