package io.taig.flog.data

import io.taig.flog.util.JsonPrinter

sealed abstract class Payload extends Product with Serializable {
  final def toJson: String = JsonPrinter(this)
}

object Payload {
  final case class Object(values: Map[String, Payload]) extends Payload {
    def isEmpty: Boolean = values.isEmpty

    def keys: Set[String] = values.keySet

    def get(key: String): Option[Payload] = values.get(key)

    def deepMerge(payload: Payload.Object): Payload.Object = {
      val keys = this.keys ++ payload.keys
      val result = collection.mutable.HashMap.empty[String, Payload]

      keys.foreach { key =>
        (this.get(key), payload.get(key)) match {
          case (Some(_: Payload.Value), Some(value: Payload.Value))      => result += (key -> value)
          case (Some(left: Payload.Object), Some(right: Payload.Object)) => result += (key -> (left deepMerge right))
          case (Some(_: Payload.Object), Some(value: Payload.Value))     => result += (key -> value)
          case (Some(_: Payload.Value), Some(value: Payload.Object))     => result += (key -> value)
          case (None, Some(value))                                       => result += (key -> value)
          case (Some(value), None)                                       => result += (key -> value)
          case (None, None)                                              => ()
        }
      }

      Payload.Object(result.toMap)
    }
  }

  final case class Value(value: String) extends Payload

  val Empty: Payload.Object = Object(Map.empty)

  def from(values: Iterable[(String, Payload)]): Payload.Object = Object(values.toMap)

  def of(values: (String, Payload)*): Payload.Object = from(values.toMap)
}
