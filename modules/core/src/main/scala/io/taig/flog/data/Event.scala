package io.taig.flog.data

import io.circe.{Encoder, Json, JsonObject}
import io.circe.syntax._
import cats.implicits._
import io.taig.flog.util.Printer

final case class Event(
    timestamp: Long,
    level: Level,
    scope: Scope,
    message: String,
    payload: Json,
    throwable: Option[Throwable]
) {
  def defaults(context: Context): Event =
    prefix(context.prefix).presets(context.presets)

  def prefix(scope: Scope): Event = copy(scope = scope ++ this.scope)

  def presets(payload: Json): Event = copy(payload = payload deepMerge this.payload)
}

object Event {
  implicit val encoder: Encoder.AsObject[Event] =
    Encoder.AsObject.instance { event =>
      JsonObject(
        "timestamp" := event.timestamp,
        "level" := event.level.show,
        "scope" := event.scope.show,
        "message" := Some(event.message).filter(_.nonEmpty),
        "payload" := event.payload,
        "stacktrace" := event.throwable.map(Printer.throwable)
      )
    }
}
