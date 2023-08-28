package io.taig.flog.slf4j2;

import org.slf4j.Marker;
import org.slf4j.event.Level;

interface LoggerRuntime {
  void log(String name, Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable);
}