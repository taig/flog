package io.taig.logging.sheets

import io.circe.Json

import scala.collection.immutable.ListMap

object SheetsJsonHelpers {
  def flatten(json: Json): ListMap[String, String] = flatten(json, key = "")

  private def flatten(json: Json, key: String): ListMap[String, String] = {
    json.fold(
      if (key.isEmpty) ListMap.empty else ListMap(key -> ""),
      value => ListMap(key -> String.valueOf(value)),
      value => ListMap(key -> value.toString),
      value => ListMap(key -> value),
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
}
