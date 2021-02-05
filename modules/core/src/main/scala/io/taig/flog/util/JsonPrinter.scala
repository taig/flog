package io.taig.flog.util

import scala.annotation.nowarn

import io.taig.flog.data.Payload

object JsonPrinter {
  def compact(payload: Payload): String = {
    val builder = new StringBuilder()
    compact(builder)(payload)
    builder.result()
  }

  private val Quote = '"'
  private val Colon = ':'
  private val Comma = ','
  private val Space = ' '
  private val Linebreak = '\n'
  private val Open = '{'
  private val Close = '}'

  @nowarn("msg=discarded non-Unit value")
  private def compact(builder: StringBuilder): Payload => Unit = {
    case Payload.Object(values) =>
      val length = values.size
      builder.append(Open)
      if (length > 0) {
        values.zipWithIndex.foreach {
          case ((_, Payload.Null), _) => ()
          case ((key, payload), index) =>
            builder.append(Quote).append(key).append(Quote).append(Colon)
            compact(builder)(payload)
            if (index < length - 1) builder.append(Comma)
        }
      }
      builder.append(Close)
    case Payload.Value(value) => builder.append(Quote).append(value).append(Quote)
    case Payload.Null         => ()
  }

  def pretty(payload: Payload): String = {
    val builder = new StringBuilder()
    pretty(builder, indent = "")(payload)
    builder.result()
  }

  @nowarn("msg=discarded non-Unit value")
  private def pretty(builder: StringBuilder, indent: String): Payload => Unit = {
    case Payload.Object(values) =>
      val shift = indent + "  "
      val length = values.size
      builder.append(Open)
      if (length > 0) {
        builder.append(Linebreak).append(shift)
        values.zipWithIndex.foreach {
          case ((_, Payload.Null), _) => ()
          case ((key, payload), index) =>
            builder.append(Quote).append(key).append(Quote).append(Colon).append(Space)
            pretty(builder, shift)(payload)
            if (index < length - 1) builder.append(Comma).append(Linebreak).append(shift) else builder.append(Linebreak)
        }
        builder.append(indent)
      }
      builder.append(Close)
    case Payload.Value(value) => builder.append(Quote).append(value).append(Quote)
    case Payload.Null         => ()
  }
}
