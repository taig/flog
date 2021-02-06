package io.taig.flog

import io.circe.{Json, JsonObject, Encoder => CirceEncoder}
import io.taig.flog.data.Payload
import cats.syntax.all._

object circe extends circe

trait circe extends circe1 {
  implicit def encoderObjectCirce[A](implicit encoder: CirceEncoder.AsObject[A]): Encoder.Object[A] = value =>
    toObject(encoder.encodeObject(value))

  def toObject(json: JsonObject): Payload.Object = Payload.Object(json.toMap.fmap(toPayload))

  def toJsonObject(payload: Payload.Object): JsonObject = JsonObject.fromMap(payload.values.fmap(toJson))
}

trait circe1 { this: circe =>
  implicit def encoderCirce[A](implicit encoder: CirceEncoder[A]): Encoder[A] = value => toPayload(encoder(value))

  def toPayload(json: Json): Payload = json.fold(
    jsonNull = Payload.Null,
    jsonBoolean = value => Payload.Value(String.valueOf(value)),
    jsonNumber = number => Payload.Value(number.toString),
    jsonString = value => Payload.Value(value),
    jsonArray = values => Payload.Object((values.indices.map(_.show) zip values.map(toPayload)).toMap),
    jsonObject = toObject
  )

  def toJson(payload: Payload): Json = payload match {
    case payload: Payload.Object => Json.fromJsonObject(toJsonObject(payload))
    case Payload.Value(value)    => Json.fromString(value)
    case Payload.Null            => Json.Null
  }
}
