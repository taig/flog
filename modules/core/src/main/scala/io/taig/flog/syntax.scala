package io.taig.flog

import io.taig.flog.data.Payload

object syntax {
  implicit class PayloadStringOps(val key: String) extends AnyVal {
    def :=[A](value: A)(implicit encoder: Encoder[A]): (String, Payload) = key -> encoder.encode(value)
  }

  implicit class PayloadAnyOps[A](val value: A) extends AnyVal {
    def asPayload(implicit encoder: Encoder[A]): Payload = encoder.encode(value)

    def asObject(implicit encoder: Encoder.Object[A]): Payload.Object = encoder.encode(value)
  }
}
