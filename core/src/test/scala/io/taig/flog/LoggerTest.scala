package io.taig.flog

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO}
import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

final class LoggerTest extends AnyWordSpec with Matchers {
  val event: Event = Event.Empty.copy(scope = Scope.Root / "foobar")

  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  def log(initial: Event)(f: Logger[IO] => Logger[IO]): IO[Scope] =
    Deferred[IO, Event].flatMap { promise =>
      val logger: Logger[IO] = Logger(event => promise.complete(event))
      f(logger)(initial) *> promise.get.map(_.scope)
    }

  "append" should {
    "to empty scope" in {
      val scope = log(event)(_.append("1")).unsafeRunSync()
      scope shouldBe Scope.Root / "1" / "foobar"
    }

    "add multiple" in {
      val scope =
        log(event)(_.append("1").append("2").append("3")).unsafeRunSync()
      scope shouldBe Scope.Root / "1" / "2" / "3" / "foobar"
    }

    "add class name" in {
      val scope = log(event)(_.append[LoggerTest]).unsafeRunSync()
      scope shouldBe Scope.Root / "LoggerTest" / "foobar"
    }
  }

  "scope" should {
    "set the scope to the full path" in {
      val scope = log(event)(_.scope[LoggerTest]).unsafeRunSync()
      scope shouldBe Scope.Root / "io" / "taig" / "flog" / "LoggerTest" / "foobar"
    }
  }
}
