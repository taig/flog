package io.taig.flog.data

import cats._
import cats.implicits._

import scala.reflect.{classTag, ClassTag}

final case class Scope(segments: List[String]) extends AnyVal {
  def isEmpty: Boolean = segments.isEmpty

  def /(segment: String): Scope =
    if (segment.isEmpty) this else Scope(segments :+ segment)

  def ++(scope: Scope): Scope = Scope(segments ++ scope.segments)

  def contains(scope: Scope): Boolean = this.show contains scope.show
}

object Scope {
  val Root: Scope = Scope(List.empty)

  def apply(root: String): Scope = Scope(List(root))

  def of(segments: String*): Scope = Scope(segments.toList)

  def fromClassName[A: ClassTag]: Scope = {
    val name = classTag[A].runtimeClass.getName
    val normalized = if (name.endsWith("$")) name.init else name
    Scope(normalized.split('.').toList)
  }

  def fromClassSimpleName[A: ClassTag]: Scope = {
    val name = classTag[A].runtimeClass.getSimpleName
    val normalized = if (name.endsWith("$")) name.init else name
    Scope.of(normalized)
  }

  implicit val monoid: Monoid[Scope] = new Monoid[Scope] {
    override def empty: Scope = Root

    override def combine(x: Scope, y: Scope): Scope = x ++ y
  }

  implicit val eq: Eq[Scope] = Eq.by(_.segments)

  implicit val show: Show[Scope] = {
    case Scope(Nil)      => "/"
    case Scope(segments) => segments.mkString(" / ")
  }
}
