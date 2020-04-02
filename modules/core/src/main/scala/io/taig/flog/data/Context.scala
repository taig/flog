package io.taig.flog.data

import java.util.UUID

import io.circe.JsonObject
import io.circe.syntax._

final case class Context(prefix: Scope, presets: JsonObject) { self =>
  def append(prefix: Scope): Context = copy(prefix = self.prefix ++ prefix)

  def combine(payload: JsonObject): Context = copy(presets = self.presets deepMerge payload)

  def trace(uuid: UUID): Context = combine(JsonObject("trace" := uuid))
}

object Context {
  val Empty: Context = Context(Scope.Root, JsonObject.empty)
}
