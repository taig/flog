package io.taig.flog

import java.util.UUID

import scala.annotation.nowarn

import cats.syntax.all._
import io.taig.flog.data.Payload
import simulacrum.typeclass

@FunctionalInterface
trait Encoder[A] {
  def encode(value: A): Payload

  final def contramap[B](f: B => A): Encoder[B] = value => encode(f(value))
}

object Encoder {
  @FunctionalInterface
  trait Object[A] {
    def encode(value: A): Payload.Object

    final def contramap[B](f: B => A): Encoder.Object[B] = value => encode(f(value))
  }

  object Object {
    def apply[A](implicit encoder: Encoder.Object[A]): Encoder.Object[A] = encoder

    implicit val obj: Encoder.Object[Payload.Object] = identity

    implicit def map[A](implicit encoder: Encoder[A]): Encoder.Object[Map[String, A]] = values =>
      Payload.Object(values.fmap(encoder.encode))
  }

  def apply[A](implicit encoder: Encoder[A]): Encoder[A] = encoder

  implicit def obj[A](implicit encoder: Encoder.Object[A]): Encoder[A] = encoder.encode(_)

  implicit val value: Encoder[Payload.Value] = identity

  implicit val payload: Encoder[Payload] = identity

  implicit val string: Encoder[String] = Payload.Value.apply

  implicit def option[A](implicit encoder: Encoder[A]): Encoder[Option[A]] = {
    case Some(value) => encoder.encode(value)
    case None        => Payload.Null
  }

  implicit val uuid: Encoder[UUID] = Encoder[String].contramap(_.toString)

  implicit val int: Encoder[Int] = Encoder[String].contramap(String.valueOf)

  implicit val long: Encoder[Long] = Encoder[String].contramap(String.valueOf)

  implicit val short: Encoder[Short] = Encoder[String].contramap(String.valueOf)
}
