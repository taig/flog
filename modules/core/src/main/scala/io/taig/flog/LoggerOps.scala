package io.taig.flog

import cats.syntax.all._
import io.taig.flog.data._

abstract class LoggerOps[F[_[_]], G[_]] {
  def modify(f: List[Event] => List[Event]): F[G]

  final def filter(filter: Event => Boolean): F[G] = modify(_.filter(filter))

  final def minimum(level: Level): F[G] = filter(_.level >= level)

  final def defaults(context: Context): F[G] = modify(_.map(_.defaults(context)))

  /** Prefix all events of this logger with the given `Scope` */
  final def prefix(scope: Scope): F[G] = defaults(Context(scope, Payload.Empty))

  final def presets(payload: Payload.Object): F[G] = defaults(Context(Scope.Root, payload))
}
