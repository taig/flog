package io.taig.flog

import java.time.Instant

import cats.implicits._
import cats.{Eval, Show}
import io.circe.{Json, JsonObject}
import io.taig.flog.internal.Shows._

final case class Event(
    level: Level,
    scope: Scope,
    timestamp: Instant,
    message: Eval[String],
    payload: Eval[JsonObject],
    throwable: Option[Throwable]
)

object Event {
  implicit val show: Show[Event] = { event =>
    val builder = new StringBuilder()

    builder
      .append('[')
      .append(event.timestamp.show)
      .append("][")
      .append(event.level.show)
      .append("][")
      .append(event.scope.show)
      .append("] ")
      .append(event.message.value)

    val payload = event.payload.value

    if (payload.nonEmpty)
      builder.append('\n').append(Json.fromJsonObject(payload).spaces2)

    event.throwable.map(_.show).foreach { value =>
      builder.append('\n').append(value)
    }

    builder.append('\n').toString
  }
}
