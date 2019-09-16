package io.taig.flog.stackdriver.interal

import cats.implicits._
import io.circe.{Json, JsonObject}

object Circe {
  final def toMap(json: JsonObject): Map[String, Any] = json.toMap.fmap(toAny)

  final def toAny(json: Json): Any = json.fold(
    jsonNull = null,
    jsonBoolean = identity,
    jsonNumber = _.toDouble,
    jsonString = identity,
    jsonArray = _.toArray.map(toAny),
    jsonObject = toMap
  )
}
