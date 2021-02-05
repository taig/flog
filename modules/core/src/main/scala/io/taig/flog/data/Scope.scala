package io.taig.flog.data

import scala.reflect.{classTag, ClassTag}

import cats._
import cats.syntax.all._
import io.taig.flog.Encoder

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

  private def fromName(value: Class[_]): Scope = {
    val name = value.getName
    val normalized = if (name.endsWith("$")) name.init else name
    Scope(normalized.split('.').toList)
  }

  def fromName(value: Any): Scope = fromName(value.getClass)

  def fromName[A: ClassTag]: Scope = fromName(classTag[A].runtimeClass)

  private def fromSimpleName(value: Class[_]): Scope = {
    val name = value.getSimpleName
    val normalized = if (name.endsWith("$")) name.init else name
    Scope.of(normalized)
  }

  def fromSimpleName(value: Any): Scope = fromSimpleName(value.getClass)

  def fromSimpleName[A: ClassTag]: Scope = fromSimpleName(classTag[A].runtimeClass)

  implicit val monoid: Monoid[Scope] = new Monoid[Scope] {
    override def empty: Scope = Root

    override def combine(x: Scope, y: Scope): Scope = x ++ y
  }

  implicit val eq: Eq[Scope] = Eq.by(_.segments)

  implicit val show: Show[Scope] = {
    case Scope(Nil)      => "/"
    case Scope(segments) => segments.mkString(" / ")
  }

  implicit val encoder: Encoder[Scope] = Encoder[String].contramap(_.show)
}
