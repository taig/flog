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
  def prefix(scope: Scope): Event = copy(scope = scope ++ this.scope)
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
