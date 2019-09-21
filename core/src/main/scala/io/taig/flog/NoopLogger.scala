package io.taig.flog

import cats.Applicative

/**
  * Ignore all events
  */
object NoopLogger {
  def apply[F[_]](implicit F: Applicative[F]): Logger[F] = Logger(_ => F.unit)
}
