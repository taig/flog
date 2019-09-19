package io.taig.flog.internal

import java.io.{PrintWriter, StringWriter}
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import cats.Show

private[flog] object Shows {
  implicit val showThrowable: Show[Throwable] = { throwable =>
    val writer = new StringWriter
    throwable.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  private val formatter: DateTimeFormatter =
    DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
      .withZone(ZoneOffset.UTC)

  implicit def showTemporalAccessor[A <: TemporalAccessor]: Show[A] =
    formatter.format
}
