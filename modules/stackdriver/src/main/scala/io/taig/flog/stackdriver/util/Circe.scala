package io.taig.flog.stackdriver.util

import java.util

import cats.implicits._
import io.circe.{Json, JsonObject}

import scala.jdk.CollectionConverters._

object Circe {
  final def toJavaMap(json: JsonObject): util.Map[String, Object] =
    json.filter { case (_, value) => !value.isNull }.toMap.fmap(toAny).asJava

  final def toAny(json: Json): Object = json.fold(
    jsonNull = Json.Null,
    jsonBoolean = Boolean.box,
    jsonNumber = number => Double.box(number.toDouble),
    jsonString = identity,
    jsonArray = _.toArray.map(toAny),
    jsonObject = toJavaMap
  )
}
