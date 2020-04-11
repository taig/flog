package io.taig.flog.http4s

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import io.taig.flog.ContextualLogger
import io.taig.flog.data.Scope
import org.http4s.{HttpApp, Request, Response}

object TracingMiddleware {
  val RequestScope: Scope = Scope.Root / "server" / "request"

  val ResponseScope: Scope = Scope.Root / "server" / "response"

  def apply[F[_]](
      logger: ContextualLogger[F]
  )(service: HttpApp[F], filter: Request[F] => Boolean = (_: Request[F]) => true)(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      val run =
        if (filter(request))
          for {
            _ <- logger.info(RequestScope, payload = encode(request))
            response <- service.run(request)
            _ <- logger.info(ResponseScope, payload = encode(response))
          } yield response
        else service.run(request)

      F.delay(UUID.randomUUID()).flatMap(uuid => logger.locally(_.trace(uuid))(run))
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
