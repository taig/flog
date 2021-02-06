package io.taig.flog

import scala.concurrent.ExecutionContext
import cats.effect.{Clock, ConcurrentEffect, Resource, Sync, Timer}
import cats.syntax.all._
import io.taig.flog.data.Level
import io.taig.flog.http4s.{CorrelationMiddleware, LoggingMiddleware}
import io.taig.flog.interop.zio.contextualZioLogger
import io.taig.flog.slf4j.FlogSlf4jBinder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpApp, Response, Status}
import zio._
import zio.interop.catz.CatsApp

object SampleApp extends CatsApp {
  def app[F[_]: Sync](logger: Logger[F]): HttpApp[F] =
    HttpApp[F] { request =>
      logger.info("I'm handling a request here, and a trace information is automagically attached to my payload!") *>
        Response[F](Status.Ok).withEntity(request.uri.show).pure[F]
    }

  def server[F[_]: ConcurrentEffect: Timer](logger: ContextualLogger[F]): Resource[F, Server[F]] =
    BlazeServerBuilder[F](ExecutionContext.global)
      .bindHttp(host = "0.0.0.0")
      .withHttpApp(CorrelationMiddleware(logger)(LoggingMiddleware(logger)(app[F](logger))))
      .resource

  def logger[F[_]: ConcurrentEffect: Clock]: F[Logger[F]] = Logger
    .stdOut[F]
    .map(_.minimum(Level.Info))
    .flatTap(FlogSlf4jBinder.initialize[F])

  override def run(arguments: List[String]): URIO[ZEnv, ExitCode] = {
    import zio.interop.catz._
    import zio.interop.catz.implicits._

    (for {
      logger <- Resource.liftF(logger[Task])
      contextual <- Resource.liftF(contextualZioLogger(logger))
      _ <- server[Task](contextual)
    } yield ExitCode.success).use(_ => Task.never).orDie
  }
}
