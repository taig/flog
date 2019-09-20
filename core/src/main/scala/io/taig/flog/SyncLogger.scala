package io.taig.flog

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.Times

object SyncLogger {
  def apply[F[_]: Sync](f: Event => F[Unit]) = Logger { event =>
    Times.now[F].flatMap(timestamp => f(event(timestamp)))
  }
}
