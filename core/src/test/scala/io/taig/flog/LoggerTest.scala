package io.taig.flog

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import io.taig.flog.data.{Event, Scope}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext

final class LoggerTest extends AsyncWordSpec with Matchers {
  val event: Long => Event = data.Event(
    _,
    Level.Info,
    Scope.Root / "foobar",
    message = "",
    payload = JsonObject.empty,
    throwable = None
  )

  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def log(
      event: Long => Event,
      prefix: Scope = Scope.Root,
      presets: JsonObject = JsonObject.empty
  )(f: Logger[IO] => Logger[IO]): IO[Event] =
    Deferred[IO, Event].flatMap { promise =>
      val logger: Logger[IO] = new Logger[IO](Scope.Root, JsonObject.empty) {
        override def log(f: Long => Event): IO[Unit] =
          promise.complete(f(0))
      }

      f(logger)(event) *> promise.get
    }

  "append" should {
    "to empty scope" in {
      log(event)(_.append("1"))
        .map { event =>
          event.scope shouldBe Scope.Root / "1" / "foobar"
        }
        .unsafeToFuture()
    }

    "add multiple" in {
      log(event)(_.append("1").append("2").append("3"))
        .map { event =>
          event.scope shouldBe Scope.Root / "1" / "2" / "3" / "foobar"
        }
        .unsafeToFuture()
    }

    "add class name" in {
      log(event)(_.append[LoggerTest])
        .map { event =>
          event.scope shouldBe Scope.Root / "LoggerTest" / "foobar"
        }
        .unsafeToFuture()
    }
  }

  "scope" should {
    "set the scope to the full path" in {
      log(event)(_.scope[LoggerTest])
        .map { event =>
          event.scope shouldBe Scope.Root / "io" / "taig" / "flog" / "LoggerTest" / "foobar"
        }
        .unsafeToFuture()
    }
  }

  "traced" should {
    "add a UUID" in {
      Tracer
        .uuid[IO]
        .flatMap { uuid =>
          log(event)(_.trace(uuid)).map { event =>
            event.payload shouldBe JsonObject("trace" := uuid)
          }
        }
        .unsafeToFuture()
    }
  }
}
