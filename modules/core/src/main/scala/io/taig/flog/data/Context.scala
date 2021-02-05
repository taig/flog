package io.taig.flog.data

import java.util.UUID

import io.taig.flog.syntax._

final case class Context(prefix: Scope, presets: Payload.Object) {
  def append(prefix: Scope): Context = copy(prefix = this.prefix ++ prefix)

  def combine(payload: Payload.Object): Context = copy(presets = this.presets deepMerge payload)

  def trace(uuid: UUID): Context = combine(Payload.of("trace" := uuid))
}

object Context {
  val Empty: Context = Context(Scope.Root, Payload.Empty)
}
