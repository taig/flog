package io.taig.flog

import java.util.UUID

import io.taig.flog.data.Payload
import simulacrum.typeclass

@typeclass
trait Encoder[A] {
  def encode(value: A): Payload

  final def contramap[B](f: B => A): Encoder[B] = value => encode(f(value))
}

object Encoder {
  implicit val obj: Encoder[Payload.Object] = identity

  implicit val value: Encoder[Payload.Value] = identity

  implicit val payload: Encoder[Payload] = identity

  implicit val string: Encoder[String] = Payload.Value.apply

  implicit val uuid: Encoder[UUID] = Encoder[String].contramap(_.toString)
}
