package io.taig.flog.data

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
