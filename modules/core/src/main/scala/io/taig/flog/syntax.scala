package io.taig.flog

import io.taig.flog.data.Payload

object syntax {
  implicit class PayloadStringOps(val key: String) extends AnyVal {
    def :=[A](value: A)(implicit encoder: PayloadEncoder[A]): (String, Payload) = key -> encoder.encode(value)
  }
}
