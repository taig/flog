package io.taig.flog.util

import io.taig.flog.data.Payload

object PayloadFlatten {
  def apply(payload: Payload.Object): Map[String, String] =
    payload.values
      .flatMap { case (key, payload) => flatten(key)(payload) }
      .collect { case (key, Some(payload)) => key -> payload }

  private def flatten(key: String): Payload => Map[String, Option[String]] = {
    case Payload.Null           => Map(key -> None)
    case Payload.Value(value)   => Map(key -> Some(value))
    case Payload.Object(values) => values.flatMap { case (next, payload) => flatten(s"$key.$next")(payload) }
  }
}
