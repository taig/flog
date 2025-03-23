package io.taig.flog.slf4j2

import org.slf4j.ILoggerFactory
import org.slf4j.helpers.NOP_FallbackServiceProvider

class FlogSlf4j2ServiceProvider extends NOP_FallbackServiceProvider:
  override def getLoggerFactory(): ILoggerFactory = new FlogSlf4j2Logger(_)
