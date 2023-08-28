package io.taig.flog.slf4j2;

import org.slf4j.ILoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class LoggerFactory implements ILoggerFactory {
  final private Map<String, Logger> loggers = new ConcurrentHashMap<>();

  private LoggerRuntime runtime = null;

  void attacheRuntime(LoggerRuntime runtime) {
    this.runtime = runtime;
  }

  void log(String name, Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
    if (runtime != null) {
      runtime.log(name, level, marker, messagePattern, arguments, throwable);
    }
  }

  @Override
  public Logger getLogger(String name) {
    return loggers.computeIfAbsent(name, n -> new Logger(n, this));
  }
}