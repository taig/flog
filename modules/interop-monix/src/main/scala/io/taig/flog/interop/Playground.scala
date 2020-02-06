package io.taig.flog.interop

import _root_.monix.eval._

import java.util.UUID

import cats.effect.ExitCode
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import io.taig.flog.data.Scope
import io.taig.flog.interop.monix._

import scala.io.Source

object Playground extends TaskApp {
  def loadWebsite(url: String, logger: Logger[Task]): Task[String] =
    for {
      _ <- logger.info(
        Scope.Root / "request",
        message = url
      )
      body <- Task(Source.fromURL(url))
        .bracket(source => Task(source.mkString))(source => Task(source.close())
        )
      _ <- logger.info(
        Scope.Root / "response",
        message = url,
        payload = JsonObject("body" -> (body.take(100) + "...").asJson)
      )
    } yield body

  def app(logger: Logger[Task]): Task[Unit] =
    (loadWebsite(url = "https://typelevel.org", logger) *>
      loadWebsite(url = "foobar", logger)).void
      .onErrorHandleWith { throwable =>
        logger.error(message = "Execution failed", throwable = throwable.some)
      }

  override def run(args: List[String]): Task[ExitCode] =
    (for {
      // Pick a simple std out logger ...
      stdOutLogger <- Logger.stdOut[Task]
      // ... and lift it into contextual mode (which is only possible with
      // monix.eval.Task and ZIO)
      contextualLogger <- contextualMonixLogger(stdOutLogger)
      uuid <- Task(UUID.randomUUID())
      _ <- contextualLogger.locally(_.trace(uuid))(app(contextualLogger))
    } yield ExitCode.Success).executeWithOptions(_.enableLocalContextPropagation)
}
