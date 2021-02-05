package io.taig.flog.util

import java.io.{PrintWriter, StringWriter}
import java.text.SimpleDateFormat
import java.util.TimeZone

import cats.syntax.all._
import io.circe.Json
import io.taig.flog.data.Event

object Printer {
  val throwable: Throwable => String = { throwable =>
    val writer = new StringWriter
    throwable.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  private val formatter: SimpleDateFormat = {
    val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    formatter
  }

  val timestamp: Long => String = formatter.format(_: Long)

  val event: Event => String = { event =>
    val builder = new StringBuilder()

    builder
      .append('[')
      .append(Printer.timestamp(event.timestamp))
      .append("][")
      .append(event.level.show)
      .append("][")
      .append(event.scope.show)
      .append("] ")
      .append(event.message)

    if (!event.payload.isEmpty)
      builder.append('\n').append(Json.fromJsonObject(event.payload).spaces2)

    event.throwable.map(Printer.throwable).foreach { value => builder.append('\n').append(value) }

    builder.append('\n').toString
  }
}
