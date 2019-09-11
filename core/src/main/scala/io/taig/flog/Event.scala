package io.taig.flog

import java.time.Instant

import cats.Eval

final case class Event(
    level: Level,
    scope: Scope,
    timestamp: Instant,
    message: Eval[String],
    payload: Eval[Map[String, String]],
    throwable: Option[Throwable]
)
