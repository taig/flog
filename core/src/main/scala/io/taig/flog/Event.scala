package io.taig.flog

import java.time.Instant

import cats.Eval
import io.circe.JsonObject

final case class Event(
    level: Level,
    scope: Scope,
    timestamp: Instant,
    message: Eval[String],
    payload: Eval[JsonObject],
    throwable: Option[Throwable]
)
