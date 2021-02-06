package io.taig.flog

import cats.syntax.all._
import io.taig.flog.data._

abstract class LoggerOps[F[_[_]], G[_]] {
  def modify(f: List[Event] => List[Event]): F[G]

  final def modifyEvent(f: Event => Event): F[G] = modify(_.map(f))

  final def modifyPayload(f: Payload.Object => Payload.Object): F[G] =
    modifyEvent(event => event.copy(payload = f(event.payload)))

  final def modifyScope(f: Scope => Scope): F[G] = modifyEvent(event => event.copy(scope = f(event.scope)))

  final def filter(filter: Event => Boolean): F[G] = modify(_.filter(filter))

  final def minimum(level: Level): F[G] = filter(_.level >= level)

  final def withDefaults(context: Context): F[G] = modifyEvent(_.defaults(context))

  /** Prefix all events of this logger with the given `Scope` */
  final def prefix(scope: Scope): F[G] = withDefaults(Context(scope, Payload.Empty))

  final def presets(payload: Payload.Object): F[G] = withDefaults(Context(Scope.Root, payload))
}
