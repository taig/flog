package io.taig.flog.http4s

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.ContextualLogger
import org.http4s.HttpApp

object TracingMiddleware {
  def apply[F[_]](logger: ContextualLogger[F])(app: HttpApp[F])(implicit F: Sync[F]): HttpApp[F] =
    HttpApp[F] { request =>
      F.delay(UUID.randomUUID()).flatMap(uuid => logger.locally(_.trace(uuid))(app.run(request)))
    }
}
