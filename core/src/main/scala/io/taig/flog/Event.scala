package io.taig.flog

import io.circe.JsonObject

final case class Event(
    timestamp: Long,
    level: Level,
    scope: Scope,
    message: String,
    payload: JsonObject,
    throwable: Option[Throwable]
)
