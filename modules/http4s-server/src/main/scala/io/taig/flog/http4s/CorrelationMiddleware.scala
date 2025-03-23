package io.taig.flog.http4s

import cats.effect.Sync
import cats.syntax.all.*
import io.taig.flog.ContextualLogger
import org.http4s.Http

import java.util.UUID

object CorrelationMiddleware {
  def apply[F[_], G[_]](logger: ContextualLogger[F])(http: Http[F, G])(implicit F: Sync[F]): Http[F, G] =
    Http[F, G] { request =>
      F.delay(UUID.randomUUID()).flatMap(uuid => logger.local(_.correlation(uuid))(http.run(request)))
    }
}
