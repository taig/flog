package io.taig.flog.http4s

import cats.effect.implicits._
import cats.effect.{ExitCase, Sync}
import cats.syntax.all._
import io.taig.flog.Logger
import io.taig.flog.data.Scope
import io.taig.flog.http4s.implicits._
import io.taig.flog.syntax._
import org.http4s.HttpApp

object LoggingMiddleware {
  def apply[F[_]: Sync](logger: Logger[F])(app: HttpApp[F]): HttpApp[F] =
    create(Logger.prefix(Scope.Root / "server")(logger), app)

  private def create[F[_]](logger: Logger[F], service: HttpApp[F])(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      (for {
        _ <- logger.info("Request", request.asObject)
        response <- service.run(request)
        _ <- logger.info("Response", response.asObject)
      } yield response).guaranteeCase {
        case ExitCase.Completed        => F.unit
        case ExitCase.Error(throwable) => logger.error("Request failed", throwable)
        case ExitCase.Canceled         => logger.info("Request cancelled", request.asObject)
      }
    }
}
