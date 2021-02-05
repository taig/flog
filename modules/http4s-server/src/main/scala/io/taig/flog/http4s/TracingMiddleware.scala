package io.taig.flog.http4s

import java.util.UUID

import cats.effect.Sync
import cats.syntax.all._
import io.taig.flog.ContextualLogger
import org.http4s.HttpApp

object TracingMiddleware {
  def apply[F[_]](logger: ContextualLogger[F])(app: HttpApp[F])(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      F.delay(UUID.randomUUID()).flatMap(uuid => logger.locally(app.run(request))(_.trace(uuid)))
    }
}
