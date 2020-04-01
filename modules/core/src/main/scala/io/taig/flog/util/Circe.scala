package io.taig.flog.util

import scala.collection.immutable.ListMap

import io.circe.Json

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
}
