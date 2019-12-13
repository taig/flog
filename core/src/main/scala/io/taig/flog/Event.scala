package io.taig.flog

import java.time.Instant

import cats.implicits._
import io.circe.{Json, JsonObject}
import io.taig.flog.internal.Shows._

final case class Event(
    level: Level,
    scope: Scope,
    message: String,
    payload: JsonObject,
    throwable: Option[Throwable]
)

object Event {
  val Empty: Event = Event(
    Level.Info,
    Scope.Root,
    message = "",
    payload = JsonObject.empty,
    throwable = None
  )

  def render(timestamp: Instant, event: Event): String = {
    val builder = new StringBuilder()

    builder
      .append('[')
      .append(timestamp.show)
      .append("][")
      .append(event.level.show)
      .append("][")
      .append(event.scope.show)
      .append("] ")
      .append(event.message)

    if (event.payload.nonEmpty)
      builder.append('\n').append(Json.fromJsonObject(event.payload).spaces2)

    event.throwable.map(_.show).foreach { value =>
      builder.append('\n').append(value)
    }

    builder.append('\n').toString
  }
}
