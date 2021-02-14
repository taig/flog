package io.taig.flog

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.all._
import io.taig.flog.data.Event
import munit.CatsEffectSuite

final class LoggerTest extends CatsEffectSuite {
  test("queued flushes before close") {
    Ref[IO]
      .of(List.empty[Event])
      .flatTap(target => Logger.queued[IO](Logger.list[IO](target)).use(_.info("foobar")))
      .map(target => assertIO(obtained = target.get.map(_.length), returns = 1))
  }

  test("batched flushes before close") {
    Ref[IO]
      .of(List.empty[Event])
      .flatTap(target => Logger.batched[IO](Logger.list[IO](target), buffer = 5).use(_.info("foobar")))
      .map(target => assertIO(obtained = target.get.map(_.length), returns = 1))
  }
}
