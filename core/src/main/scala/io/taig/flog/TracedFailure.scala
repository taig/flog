package io.taig.flog

import java.util.UUID

import cats.implicits._
import io.circe.JsonObject

final case class TracedFailure[F[_]](
    prefix: Scope,
    preset: JsonObject,
    trace: UUID,
    cause: Throwable
) extends Exception(show"Traced failure with id $trace", cause)
