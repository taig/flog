package io.taig.flog

import cats.effect.IO
import cats.effect.concurrent.Deferred
import cats.effect.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.matchers.should.Matchers

final class LoggerTest extends AsyncIOSpec with Matchers {
  val event: Event = Event.Empty.copy(scope = Scope.Root / "foobar")

  def log(initial: Event)(f: Logger[IO] => Logger[IO]): IO[Event] =
    Deferred[IO, Event].flatMap { promise =>
      val logger: Logger[IO] = Logger(event => promise.complete(event))
      f(logger)(initial) *> promise.get
    }

  "append" - {
    "to empty scope" in {
      log(event)(_.append("1"))
        .asserting(_.scope shouldBe Scope.Root / "1" / "foobar")
    }

    "add multiple" in {
      log(event)(_.append("1").append("2").append("3"))
        .asserting(_.scope shouldBe Scope.Root / "1" / "2" / "3" / "foobar")
    }

    "add class name" in {
      log(event)(_.append[LoggerTest])
        .asserting(_.scope shouldBe Scope.Root / "LoggerTest" / "foobar")
    }
  }

  "scope" - {
    "set the scope to the full path" in {
      log(event)(_.scope[LoggerTest])
        .asserting(
          _.scope shouldBe Scope.Root / "io" / "taig" / "flog" / "LoggerTest" / "foobar"
        )
    }
  }
}
