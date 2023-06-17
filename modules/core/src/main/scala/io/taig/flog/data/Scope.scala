package io.taig.flog.data

import scala.reflect.{classTag, ClassTag}
import cats.*
import cats.data.Chain
import cats.syntax.all.*
import io.circe.Encoder

opaque type Scope = Chain[String]

object Scope:
  extension (self: Scope)
    def isEmpty: Boolean = self.isEmpty
    def /(segment: String): Scope = if segment.isEmpty then self else Scope(self :+ segment)
    def ++(scope: Scope): Scope = Scope(self ++ scope.toChain)
    def contains(scope: Scope): Boolean = self.show `contains` scope.show
    def startsWith(segment: String): Boolean = self.headOption.contains(segment)
    def endsWith(segment: String): Boolean = self.lastOption.contains(segment)
    def toChain: Chain[String] = self
    def toList: List[String] = self.toList
    def toString: String = self.show

  val Root: Scope = Scope(Chain.empty)

  def apply(segments: Chain[String]): Scope = segments
  def one(root: String): Scope = Chain.one(root)
  def from(segments: Iterable[String]): Scope = Chain.fromSeq(segments.toSeq)
  def of(segments: String*): Scope = Chain.fromSeq(segments)

  private def fromName(value: Class[?]): Scope =
    val name = value.getName
    val normalized = if name.endsWith("$") then name.init else name
    Scope.from(normalized.split('.'))

  def fromName(value: Any): Scope = fromName(value.getClass)

  def fromName[A: ClassTag]: Scope = fromName(classTag[A].runtimeClass)

  private def fromSimpleName(value: Class[?]): Scope =
    val name = value.getSimpleName
    val normalized = if name.endsWith("$") then name.init else name
    Scope.of(normalized)

  def fromSimpleName(value: Any): Scope = fromSimpleName(value.getClass)

  def fromSimpleName[A: ClassTag]: Scope = fromSimpleName(classTag[A].runtimeClass)

  given Monoid[Scope] with
    override def empty: Scope = Root
    override def combine(x: Scope, y: Scope): Scope = x ++ y

  given (using eq: Eq[Chain[String]]): Eq[Scope] = eq
  given Show[Scope] = _.toChain match
    case Chain.nil => "/"
    case segments  => segments.mkString_(" / ")
  given Encoder[Scope] = Encoder[String].contramap(_.show)
