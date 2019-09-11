package io.taig.logging

import cats._
import cats.implicits._

final case class Scope(segments: List[String]) extends AnyVal {
  def /(segment: String): Scope =
    if (segment.isEmpty) this else Scope(segments :+ segment)

  def ++(scope: Scope): Scope = Scope(segments ++ scope.segments)

  def contains(scope: Scope): Boolean =
    this.show contains scope.show
}

object Scope {
  val Root: Scope = Scope(List.empty)

  def apply(root: String): Scope = Scope(List(root))

  def of(segments: String*): Scope = Scope(segments.toList)

  implicit val monoid: Monoid[Scope] = new Monoid[Scope] {
    override def empty: Scope = Root

    override def combine(x: Scope, y: Scope): Scope = x ++ y
  }

  implicit val show: Show[Scope] = {
    case Scope(Nil)      => "/"
    case Scope(segments) => segments.mkString(" / ")
  }
}
