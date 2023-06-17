package io.taig.flog

import cats.syntax.all.*
import io.circe.JsonObject
import io.taig.flog.data.*

abstract class LoggerOps[F[_[_]], G[_]]:
  def modify(f: List[Event] => List[Event]): F[G]

  final def modifyEvent(f: Event => Event): F[G] = modify(_.map(f))

  final def filter(filter: Event => Boolean): F[G] = modify(_.filter(filter))

  final def minimum(level: Level): F[G] = filter(_.level >= level)

  /** Append the given `Scope` to all events of this logger */
  final def append(scope: Scope): F[G] = modifyEvent(_.append(scope))

  /** Prepend the given `Scope` to all events of this logger */
  final def prepend(scope: Scope): F[G] = modifyEvent(_.prepend(scope))

  final def merge(payload: JsonObject): F[G] = modifyEvent(_.merge(payload))

  final def withContext(context: Context): F[G] = modifyEvent(_.withContext(context))
