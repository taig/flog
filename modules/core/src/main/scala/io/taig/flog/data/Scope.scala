package io.taig.flog.data

import scala.reflect.{classTag, ClassTag}
import cats._
import cats.data.Chain
import cats.syntax.all._
import io.circe.Encoder

final case class Scope(segments: Chain[String]) extends AnyVal {
  def isEmpty: Boolean = segments.isEmpty

  def /(segment: String): Scope = if (segment.isEmpty) this else Scope(segments :+ segment)

  def ++(scope: Scope): Scope = Scope(segments ++ scope.segments)

  def contains(scope: Scope): Boolean = this.show contains scope.show

  def startsWith(segment: String): Boolean = segments.headOption.contains(segment)

  def endsWith(segment: String): Boolean = segments.lastOption.contains(segment)

  def toList: List[String] = segments.toList

  override def toString: String = segments match {
    case Chain.nil => "/"
    case segments  => segments.mkString_(" / ")
  }
}

object Scope {
  val Root: Scope = Scope(Chain.empty)

  def one(root: String): Scope = Scope(Chain.one(root))

  def from(segments: Iterable[String]): Scope = Scope(Chain.fromSeq(segments.toSeq))

  def of(segments: String*): Scope = Scope(Chain.fromSeq(segments))

  private def fromName(value: Class[_]): Scope = {
    val name = value.getName
    val normalized = if (name.endsWith("$")) name.init else name
    Scope.from(normalized.split('.'))
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

  implicit val show: Show[Scope] = Show.fromToString

  implicit val encoder: Encoder[Scope] = Encoder[String].contramap(_.show)
}
