package io.taig.flog.data

import java.util.UUID

import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.util.Circe

final case class Context(prefix: Scope, payload: JsonObject) { self =>
  def append(prefix: Scope): Context =
    copy(prefix = self.prefix ++ prefix)

  def combine(payload: JsonObject): Context =
    copy(payload = Circe.combine(self.payload, payload))

  def trace(uuid: UUID): Context = combine(JsonObject("trace" := uuid))
}

object Context {
  val Empty: Context = Context(Scope.Root, JsonObject.empty)
}
