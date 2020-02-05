package io.taig.flog.data

import io.circe.JsonObject
import io.taig.flog.util.Circe

final case class Context(prefix: Scope, payload: JsonObject) { self =>
  def append(prefix: Scope): Context =
    copy(prefix = self.prefix ++ prefix)

  def combine(payload: JsonObject): Context =
    copy(payload = Circe.combine(self.payload, payload))
}

object Context {
  val Empty: Context = Context(Scope.Root, JsonObject.empty)
}
