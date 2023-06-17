package io.taig.flog

import cats.effect.*
import cats.effect.std.Dispatcher
import io.taig.flog.data.Level
import io.taig.flog.http4s.{CorrelationMiddleware, LoggingMiddleware}
import io.taig.flog.slf4j.FlogSlf4jBinder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.dsl.io.*
import com.comcast.ip4s.*

object SampleApp extends ResourceApp.Forever:
  def app(logger: Logger[IO]): HttpApp[IO] = HttpRoutes
    .of[IO]:
      case GET -> Root / "crash" => IO.raiseError(new RuntimeException("💣"))
      case GET -> Root =>
        logger.info("I'm handling a request here, and a trace information is automagically attached to my payload!") *>
          Ok()
    .orNotFound

  def server(logger: ContextualLogger[IO]): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(host"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(CorrelationMiddleware(logger)(LoggingMiddleware(logger)(app(logger))))
    .build

  val logger: Resource[IO, Logger[IO]] = Dispatcher
    .parallel[IO]
    .flatMap: dispatcher =>
      Resource
        .eval(Logger.stdOut[IO])
        .flatMap(Logger.queued[IO])
        .map(_.minimum(Level.Info))
        .evalTap(FlogSlf4jBinder.initialize(_, dispatcher))

  override def run(arguments: List[String]): Resource[IO, Unit] = for
    logger <- logger
    contextual <- Resource.eval(ContextualLogger.ofIO(logger))
    _ <- server(contextual)
  yield ()
