package io.taig.flog.util

import cats.syntax.all._
import io.taig.flog.data.Event

object EventPrinter {
  private val Linebreak = '\n'
  private val Space = ' '
  private val Open = '['
  private val Close = ']'

  def apply(event: Event): String = {
    val builder = new StringBuilder()

    builder
      .append(Open)
      .append(TimestampPrinter(event.timestamp))
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

    event.throwable.map(StacktracePrinter(_)).foreach(value => builder.append(Linebreak).append(value))

    builder.append(Linebreak).toString
  }
}
