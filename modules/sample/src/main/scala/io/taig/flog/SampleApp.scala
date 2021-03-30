package io.taig.flog

import cats.effect._
import cats.syntax.all._
import io.taig.flog.data.Level
import io.taig.flog.http4s.{CorrelationMiddleware, LoggingMiddleware}
import io.taig.flog.slf4j.FlogSlf4jBinder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpApp, Response, Status}

object SampleApp extends IOApp.Simple {
  def app[F[_]: Sync](logger: Logger[F]): HttpApp[F] =
    HttpApp[F] { request =>
      logger.info("I'm handling a request here, and a trace information is automagically attached to my payload!") *>
        Response[F](Status.Ok).withEntity(request.uri.show).pure[F]
    }

  def server[F[_]: Async](logger: ContextualLogger[F]): Resource[F, Server] =
    BlazeServerBuilder[F](runtime.compute)
      .bindHttp(host = "0.0.0.0")
      .withHttpApp(CorrelationMiddleware(logger)(LoggingMiddleware(logger)(app[F](logger))))
      .resource

  def logger[F[_]: Async]: Resource[F, Logger[F]] = Resource
    .eval(Logger.stdOut[F])
    .flatMap(Logger.queued[F])
    .map(_.minimum(Level.Info))
    .flatTap(FlogSlf4jBinder.initialize[F](_))

  override def run: IO[Unit] =
    (for {
      logger <- logger[IO]
      contextual <- Resource.eval(ContextualLogger.ofIO(logger))
      _ <- server[IO](contextual)
    } yield ()).use(_ => IO.never)
}
