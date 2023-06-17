package io.taig.flog.data

import io.circe.JsonObject
import io.circe.syntax.*

import java.util.UUID

final case class Context(prefix: Scope, presets: JsonObject):
  def append(prefix: Scope): Context = copy(prefix = this.prefix ++ prefix)

  def combine(payload: JsonObject): Context = copy(presets = this.presets `deepMerge` payload)

  def correlation(uuid: UUID): Context = combine(JsonObject("correlation" := uuid))

object Context:
  val Empty: Context = Context(Scope.Root, JsonObject.empty)
