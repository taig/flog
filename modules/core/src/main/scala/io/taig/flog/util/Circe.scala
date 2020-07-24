package io.taig.flog.util

import java.util

import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters._

import cats.implicits._
import io.circe.{Json, JsonObject}

object Circe {
  final def flatten(json: Json): ListMap[String, Json] =
    flatten(json, key = "")

  private def flatten(json: Json, key: String): ListMap[String, Json] =
    json.fold(
      if (key.isEmpty) ListMap.empty else ListMap(key -> Json.Null),
      value => ListMap(key -> Json.fromBoolean(value)),
      value => ListMap(key -> Json.fromJsonNumber(value)),
      value => ListMap(key -> Json.fromString(value)),
      values =>
        ListMap(values.zipWithIndex: _*).flatMap {
          case (json, index) if key.isEmpty => flatten(json, s"[$index]")
          case (json, index)                => flatten(json, s"$key[$index]")
        },
      value =>
        ListMap(value.toList: _*).flatMap {
          case (name, json) if key.isEmpty => flatten(json, name)
          case (name, json)                => flatten(json, s"$key.$name")
        }
    )

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
