package io.taig.flog.util

import java.io.{PrintWriter, StringWriter}
import java.text.SimpleDateFormat
import java.util.TimeZone

import cats.syntax.all._
import io.taig.flog.data.Event

object EventPrinter {
  private val throwable: Throwable => String = { throwable =>
    val writer = new StringWriter
    throwable.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

  private val formatter: SimpleDateFormat = {
    val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    formatter
  }

  private val timestamp: Long => String = formatter.format(_: Long)

  private val Linebreak = '\n'
  private val Space = ' '
  private val Open = '['
  private val Close = ']'

  def apply(event: Event): String = {
    val builder = new StringBuilder()

    builder
      .append(Open)
      .append(EventPrinter.timestamp(event.timestamp))
      .append(Close)
      .append(Open)
      .append(event.level.show)
      .append(Close)
      .append(Open)
      .append(event.scope.show)
      .append(Close)
      .append(Space)
      .append(event.message)

    if (!event.payload.isEmpty) builder.append(Linebreak).append(event.payload.toJson)

    event.throwable.map(EventPrinter.throwable).foreach(value => builder.append(Linebreak).append(value))

    builder.append(Linebreak).toString
  }
}
