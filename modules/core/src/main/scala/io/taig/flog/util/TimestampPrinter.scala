package io.taig.flog.util

import java.text.SimpleDateFormat
import java.util.TimeZone

object TimestampPrinter:
  private val formatter: SimpleDateFormat =
    val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    formatter

  def apply(timestamp: Long): String = formatter.format(timestamp)
