package io.taig.flog

import java.util.UUID
import cats.implicits._

final case class TracedFailure[F[_]](
    logger: Logger[F],
    trace: UUID,
    cause: Throwable
) extends Exception(show"Traced failure with id $trace", cause)
