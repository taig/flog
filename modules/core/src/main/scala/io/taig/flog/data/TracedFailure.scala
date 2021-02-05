package io.taig.flog.data

import java.util.UUID

import cats.syntax.all._
import io.circe.JsonObject

final case class TracedFailure(
    trace: UUID,
    prefix: Scope,
    presets: JsonObject,
    cause: Throwable
) extends Exception(show"Traced failure with id $trace", cause)
