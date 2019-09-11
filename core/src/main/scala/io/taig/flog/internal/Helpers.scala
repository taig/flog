package io.taig.flog.internal

import java.io.{PrintWriter, StringWriter}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

import cats.effect.Sync

object Helpers {
  def print(throwable: Throwable): String = {
    val writer = new StringWriter
    throwable.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  def timestamp[F[_]](implicit F: Sync[F]): F[Instant] = F.delay(Instant.now())

  val TimeFormatter: DateTimeFormatter =
    DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
      .withZone(ZoneOffset.UTC)
}
