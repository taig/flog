package io.taig.flog.internal

import cats.implicits._
import io.taig.flog.data.{Event, Level}

trait Filters[L[A[_]]] {
  def filter[F[_]](logger: L[F])(filter: Event => Boolean): L[F]

  final def level[F[_]](logger: L[F])(level: Level): L[F] =
    filter(logger)(_.level >= level)
}
