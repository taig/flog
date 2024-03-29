package io.taig.flog.http4s

import cats.effect.{Outcome, Sync}
import cats.effect.implicits.*
import cats.syntax.all.*
import io.taig.flog.Logger
import io.circe.syntax.*
import io.taig.flog.data.Scope
import io.taig.flog.http4s.implicits.*
import org.http4s.Http

object LoggingMiddleware {
  def apply[F[_]: Sync, G[_]](logger: Logger[F])(http: Http[F, G]): Http[F, G] =
    create(logger.prepend(Scope.one("server")), http)

  private def create[F[_], G[_]](logger: Logger[F], http: Http[F, G])(implicit F: Sync[F]): Http[F, G] =
    Http[F, G] { request =>
      (for
        _ <- logger.info("Request", request.asJsonObject)
        response <- http.run(request)
        _ <- logger.info("Response", response.asJsonObject)
      yield response).guaranteeCase {
        case Outcome.Succeeded(_)       => F.unit
        case Outcome.Errored(throwable) => logger.error("Request failed", throwable)
        case Outcome.Canceled()         => logger.info("Request cancelled", request.asJsonObject)
      }
    }
}
