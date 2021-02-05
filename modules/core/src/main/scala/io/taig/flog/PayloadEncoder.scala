package io.taig.flog

import java.util.UUID

import io.taig.flog.data.Payload
import simulacrum.typeclass

@typeclass
trait PayloadEncoder[A] {
  def encode(value: A): Payload

  final def contramap[B](f: B => A): PayloadEncoder[B] = value => encode(f(value))
}

object PayloadEncoder {
  implicit val obj: PayloadEncoder[Payload.Object] = identity

  implicit val value: PayloadEncoder[Payload.Value] = identity

  implicit val payload: PayloadEncoder[Payload] = identity

  implicit val string: PayloadEncoder[String] = Payload.Value.apply

  implicit val uuid: PayloadEncoder[UUID] = PayloadEncoder[String].contramap(_.toString)
}
