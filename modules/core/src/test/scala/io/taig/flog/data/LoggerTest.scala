package io.taig.flog.data

import cats.effect.IO
import cats.syntax.all._
import cats.effect.concurrent.Ref
import io.taig.flog.Logger
import munit.CatsEffectSuite

final class LoggerTest extends CatsEffectSuite {
  test("queued flushes before close") {
    Ref[IO]
      .of(List.empty[Event])
      .flatTap(target => Logger.queued[IO](Logger.list[IO](target)).use(_.info("foobar")))
      .map(target => assertIO(obtained = target.get.map(_.length), returns = 1))
  }
}
