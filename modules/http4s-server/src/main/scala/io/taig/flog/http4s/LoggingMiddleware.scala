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
  val LogAll: Request[Any] => Boolean = (_: Request[Any]) => true

  def apply[F[_]: Sync](logger: Logger[F])(app: HttpApp[F], filter: Request[F] => Boolean = LogAll): HttpApp[F] =
    create(Logger.prefix(Scope.Root / "server")(logger), app, filter)

  private def create[F[_]](logger: Logger[F], service: HttpApp[F], filter: Request[F] => Boolean)(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      if (filter(request))
        (for {
          _ <- logger.info("Request", payload = encode(request))
          response <- service.run(request)
          _ <- logger.info("Response", payload = encode(response))
        } yield response).guaranteeCase {
          case ExitCase.Completed => F.unit
          case ExitCase.Canceled => logger.info("Request cancelled")
          case ExitCase.Error(_) => F.unit
        }
      else service.run(request)
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
