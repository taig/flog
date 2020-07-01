package io.taig.flog.http4s

import cats.effect.{ExitCase, Sync}
import cats.effect.implicits._
import cats.implicits._
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import org.http4s.{HttpApp, Request, Response}

object LoggingMiddleware {
  def apply[F[_]: Sync](logger: Logger[F])(app: HttpApp[F]): HttpApp[F] =
    create(Logger.prefix(Scope.Root / "server")(logger), app)

  private def create[F[_]](logger: Logger[F], service: HttpApp[F])(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      (for {
        _ <- logger.info("Request", encode(request))
        response <- service.run(request)
        _ <- logger.info("Response", encode(response))
      } yield response).guaranteeCase {
        case ExitCase.Completed        => F.unit
        case ExitCase.Error(throwable) => logger.error("Request failed", throwable)
        case ExitCase.Canceled         => logger.info("Request cancelled", encode(request))
      }
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
