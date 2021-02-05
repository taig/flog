package io.taig.flog.util

import java.io.{PrintWriter, StringWriter}

object StacktracePrinter {
  def apply(throwable: Throwable): String = {
    val writer = new StringWriter
    throwable.printStackTrace(new PrintWriter(writer))
    writer.toString
  }
}
