package io.taig.flog.data

import java.util.UUID

import io.circe.Json
import io.circe.syntax._

final case class Context(prefix: Scope, presets: Json) { self =>
  def append(prefix: Scope): Context = copy(prefix = self.prefix ++ prefix)

  def combine(payload: Json): Context = copy(presets = self.presets deepMerge payload)

  def trace(uuid: UUID): Context = combine(Json.obj("trace" := uuid))
}

object Context {
  val Empty: Context = Context(Scope.Root, Json.Null)
}
