package io.taig.flog.internal

import cats.implicits._
import io.taig.flog.data.{Event, Level, Scope}

trait Builders[L[A[_]]] {
  def filter[F[_]](logger: L[F])(filter: Event => Boolean): L[F]

  final def level[F[_]](logger: L[F])(level: Level): L[F] =
    filter(logger)(_.level >= level)

  /** Prefix all events of this logger with the given `Scope` */
  def prefix[F[_]](logger: L[F])(scope: Scope): L[F]
}
