package io.taig.flog.internal

import cats.syntax.all._
import io.taig.flog.data._

trait Builders[L[A[_]]] {
  def filter[F[_]](filter: Event => Boolean)(logger: L[F]): L[F]

  final def level[F[_]](level: Level)(logger: L[F]): L[F] = filter(_.level >= level)(logger)

  def defaults[F[_]](context: Context)(logger: L[F]): L[F]

  /** Prefix all events of this logger with the given `Scope` */
  final def prefix[F[_]](scope: Scope)(logger: L[F]): L[F] =
    defaults(Context(scope, Payload.Empty))(logger)

  final def presets[F[_]](payload: Payload.Object)(logger: L[F]): L[F] =
    defaults(Context(Scope.Root, payload))(logger)
}
