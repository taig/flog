package io.taig.logging

import java.time.Instant

import cats.Eval
import io.circe.Json

final case class Event(
    level: Level,
    scope: Scope,
    timestamp: Instant,
    message: Eval[String],
    payload: Eval[Json],
    throwable: Option[Throwable]
)
