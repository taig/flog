package io.taig.flog.data

import io.taig.flog.Encoder
import io.taig.flog.syntax._
import io.taig.flog.util.StacktracePrinter

final case class Event(
    timestamp: Long,
    level: Level,
    scope: Scope,
    message: String,
    payload: Payload.Object,
    throwable: Option[Throwable]
) {
  def defaults(context: Context): Event = prefix(context.prefix).presets(context.presets)

  def prefix(scope: Scope): Event = copy(scope = scope ++ this.scope)

  def presets(payload: Payload.Object): Event = copy(payload = payload deepMerge this.payload)
}

object Event {
  implicit val encoder: Encoder.Object[Event] = event =>
    Payload.of(
      "timestamp" := event.timestamp,
      "level" := event.level,
      "scope" := event.scope,
      "message" := Some(event.message).filter(_.nonEmpty),
      "payload" := event.payload,
      "stacktrace" := event.throwable.map(StacktracePrinter(_))
    )
}
