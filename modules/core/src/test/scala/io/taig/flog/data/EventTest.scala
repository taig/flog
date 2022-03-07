package io.taig.flog.data

import io.circe.JsonObject
import io.circe.syntax._
import munit.FunSuite

final class EventTest extends FunSuite {
  test("prefix: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.prepend(Scope.Root).scope, expected = Scope.Root)
  }

  test("prefix: prefix empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
    assertEquals(obtained = event.prepend(Scope.Root).scope, expected = Scope.Root / "foobar")
  }

  test("prefix: event empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.prepend(Scope.Root / "foobar").scope, expected = Scope.Root / "foobar")
  }

  test("prefix: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
    assertEquals(
      obtained = event.prepend(Scope.Root / "foo" / "bar").scope,
      expected = Scope.Root / "foo" / "bar" / "foobar"
    )
  }

  test("merge: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.merge(JsonObject.empty).payload, expected = JsonObject.empty)
  }

  test("merge: right empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "bar"), None)
    assertEquals(obtained = event.merge(JsonObject.empty).payload, expected = JsonObject("foo" := "bar"))
  }

  test("merge: left empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.merge(JsonObject("foo" := "bar")).payload, expected = JsonObject("foo" := "bar"))
  }

  test("merge: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "baz"), None)
    val right = JsonObject("foo" := "bar", "quux" := "quuz")
    val expected = JsonObject("foo" := "bar", "quux" := "quuz")
    assertEquals(obtained = event.merge(right).payload, expected = expected)
  }
}
