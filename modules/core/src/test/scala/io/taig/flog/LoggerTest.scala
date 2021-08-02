package io.taig.flog

import cats.effect.IO
import cats.effect.Ref
import io.taig.flog.data.Event
import munit.CatsEffectSuite

import scala.concurrent.duration.DurationInt

final class LoggerTest extends CatsEffectSuite {
  test("queued flushes before close") {
    Ref[IO]
      .of(List.empty[Event])
      .flatTap(target =>
        Logger.queued[IO](Logger.list[IO](target)).use { l =>
          IO.sleep(3.seconds) *>
            l.info("foobar")
        }
      )
      .map(target => assertIO(obtained = target.get.map(_.length), returns = 1))
  }

  test("batched flushes before close") {
    Ref[IO]
      .of(List.empty[Event])
      .flatTap(target => Logger.batched[IO](Logger.list[IO](target), buffer = 5).use(_.info("foobar")))
      .map(target => assertIO(obtained = target.get.map(_.length), returns = 1))
  }
}
