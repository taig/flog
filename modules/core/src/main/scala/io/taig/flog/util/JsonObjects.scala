package io.taig.flog.util

import cats.syntax.all._
import io.circe.{Json, JsonNumber, JsonObject}

import java.util.{Map => JMap}
import java.lang.{Object => JObject}
import scala.jdk.CollectionConverters._

object JsonObjects {
  private def toJavaNumber(json: JsonNumber): JObject =
    json.toInt
      .map(Int.box)
      .orElse(json.toLong.map(Long.box))
      .getOrElse(Double.box(json.toDouble))

  private def toJavaObject(json: Json): JObject = json.fold(
    jsonNull = null,
    jsonBoolean = Boolean.box,
    jsonNumber = toJavaNumber,
    jsonString = identity,
    jsonArray = _.map(toJavaObject).asJavaCollection,
    jsonObject = toJavaMap
  )

  def toJavaMap(json: JsonObject): JMap[String, JObject] = json.toMap.fmap(toJavaObject).asJava
}
