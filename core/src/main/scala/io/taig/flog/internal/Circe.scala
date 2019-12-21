package io.taig.flog.internal

import io.circe.JsonObject

object Circe {
  def combine(x: JsonObject, y: JsonObject): JsonObject =
    if (x.isEmpty) y
    else if (y.isEmpty) x
    else JsonObject.fromMap(x.toMap ++ y.toMap)
}
