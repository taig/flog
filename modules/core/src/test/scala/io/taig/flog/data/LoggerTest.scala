package io.taig.flog.data

import cats.effect.{Clock, ContextShift, IO}
import cats.implicits._
import fs2.concurrent.Queue
import io.circe.Json
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.TimeUnit

class LoggerTest extends AsyncWordSpec with Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val clock: Clock[IO] = new Clock[IO] {
    override def realTime(unit: TimeUnit): IO[Long] = IO.pure(0)

    override def monotonic(unit: TimeUnit): IO[Long] = IO.pure(0)
  }

  val logger: IO[(Logger[IO], Queue[IO, Event])] =
    for {
      queue <- Queue.unbounded[IO, Event]
      logger = Logger(_.traverse_(queue.enqueue1))
    } yield (logger, queue)

  "prefix" should {
    "prepend to the scope" in {

      logger
        .flatMap {
          case (logger, queue) =>
            Logger.prefix(Scope.Root / "foo" / "bar")(logger).info(Scope.Root / "foobar", "hello world") *>
              queue.dequeue1
        }
        .map(_.scope shouldBe Scope.Root / "foo" / "bar" / "foobar")
        .unsafeToFuture()
    }
  }

  "presets" should {
    "add defaults to the payload" in {
      val expected = Json.obj("foo" := "bar", "baz" := "qux")

      logger
        .flatMap {
          case (logger, queue) =>
            Logger.presets(Json.obj("foo" := "bar"))(logger).info(Json.obj("baz" := "qux")) *>
              queue.dequeue1
        }
        .map(_.payload shouldBe expected)
        .unsafeToFuture()
    }

    "favor the log payload on conflict" in {
      val expected = Json.obj("foo" := "baz", "quux" := "quuz")

      logger
        .flatMap {
          case (logger, queue) =>
            Logger
              .presets(Json.obj("foo" := "bar", "quux" := "quuz"))(logger)
              .info(Json.obj("foo" := "baz")) *>
              queue.dequeue1
        }
        .map(_.payload shouldBe expected)
        .unsafeToFuture()
    }
  }
}
