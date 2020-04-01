package io.taig.flog.internal

import cats.implicits._
import io.circe.Json
import io.taig.flog.data.{Event, Level, Scope}

trait Builders[L[A[_]]] {
  def filter[F[_]](filter: Event => Boolean)(logger: L[F]): L[F]

  final def level[F[_]](level: Level)(logger: L[F]): L[F] =
    filter(_.level >= level)(logger)

  /** Prefix all events of this logger with the given `Scope` */
  def prefix[F[_]](scope: Scope)(logger: L[F]): L[F]

  def preset[F[_]](payload: Json)(logger: L[F]): L[F]
}
