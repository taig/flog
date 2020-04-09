package org.slf4j.impl

import io.taig.flog.slf4j.FlogSlf4jBinder
import org.slf4j.ILoggerFactory
import org.slf4j.helpers.NOPLoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

abstract class StaticLoggerBinder extends LoggerFactoryBinder

object StaticLoggerBinder extends StaticLoggerBinder {
  final override def getLoggerFactory: ILoggerFactory =
    FlogSlf4jBinder.getFactory().getOrElse(new NOPLoggerFactory)

  final override val getLoggerFactoryClassStr: String = getClass.getName

  var REQUESTED_API_VERSION: String = "1.7.30"

  def getSingleton: StaticLoggerBinder = this
}
