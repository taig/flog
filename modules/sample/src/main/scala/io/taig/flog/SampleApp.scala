package io.taig.flog

import cats.effect.*
import com.comcast.ip4s.*
import io.taig.flog.data.Level
import io.taig.flog.http4s.CorrelationMiddleware
import io.taig.flog.http4s.LoggingMiddleware
import io.taig.flog.log4cats.Log4CatsLoggerFactory
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory

import scala.concurrent.duration.*

object SampleApp extends ResourceApp.Forever:
  def app(logger: Logger[IO]): HttpApp[IO] = HttpRoutes
    .of[IO]:
      case GET -> Root / "crash" => IO.raiseError(new RuntimeException("💣"))
      case GET -> Root =>
        logger.info("I'm handling a request here, and a trace information is automagically attached to my payload!") *>
          Ok()
    .orNotFound

  def server(logger: ContextualLogger[IO]): Resource[IO, Server] =
    given LoggerFactory[IO] = Log4CatsLoggerFactory(logger)

    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(CorrelationMiddleware(logger)(LoggingMiddleware(logger)(app(logger))))
      .withShutdownTimeout(1.second)
      .build

  val logger: Resource[IO, Logger[IO]] =
    Resource.eval(Logger.stdOut[IO]).flatMap(Logger.queued[IO]).map(_.minimum(Level.Info))

  override def run(arguments: List[String]): Resource[IO, Unit] = for
    logger <- logger
    contextual <- Resource.eval(ContextualLogger.ofIO(logger))
    _ <- server(contextual)
  yield ()
