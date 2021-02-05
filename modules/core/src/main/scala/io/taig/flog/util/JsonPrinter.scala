package io.taig.flog.util

import scala.annotation.nowarn

import io.taig.flog.data.Payload

object JsonPrinter {
  def apply(payload: Payload): String = {
    val builder = new StringBuilder()
    JsonPrinter(builder)(payload)
    builder.result()
  }

  private val Quote = '"'
  private val Colon = ':'
  private val Comma = ','
  private val Open = '{'
  private val Close = '}'

  @nowarn("msg=discarded non-Unit value")
  private def apply(builder: StringBuilder): Payload => Unit = {
    case Payload.Object(values) =>
      val length = values.size
      builder.append(Open)
      if (length > 0) {
        values.zipWithIndex.foreach { case ((key, payload), index) =>
          builder.append(Quote).append(key).append(Quote).append(Colon)
          JsonPrinter(builder)(payload)
          if (index < length - 1) builder.append(Comma)
        }
      }
      builder.append(Close)
    case Payload.Value(value) => builder.append(Quote).append(value).append(Quote)
  }
}
