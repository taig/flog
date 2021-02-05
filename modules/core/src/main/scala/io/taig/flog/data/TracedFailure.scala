package io.taig.flog.data

import java.util.UUID

import cats.syntax.all._

final case class TracedFailure(
    trace: UUID,
    prefix: Scope,
    presets: Payload.Object,
    cause: Throwable
) extends Exception(show"Traced failure with id $trace", cause)
