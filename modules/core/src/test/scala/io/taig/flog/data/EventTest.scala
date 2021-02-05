package io.taig.flog.data

import io.circe.JsonObject
import munit.FunSuite
import io.circe.syntax._

final class EventTest extends FunSuite {
  test("prefix: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.prefix(Scope.Root).scope, expected = Scope.Root)
  }

  test("prefix: prefix empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
    assertEquals(obtained = event.prefix(Scope.Root).scope, expected = Scope.Root / "foobar")
  }

  test("prefix: event empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.prefix(Scope.Root / "foobar").scope, expected = Scope.Root / "foobar")
  }

  test("prefix: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
    assertEquals(
      obtained = event.prefix(Scope.Root / "foo" / "bar").scope,
      expected = Scope.Root / "foo" / "bar" / "foobar"
    )
  }

  test("presets: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.presets(JsonObject.empty).payload, expected = JsonObject.empty)
  }

  test("presets: presets empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "bar"), None)
    assertEquals(obtained = event.presets(JsonObject.empty).payload, expected = JsonObject("foo" := "bar"))
  }

  test("presets: event empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
    assertEquals(obtained = event.presets(JsonObject("foo" := "bar")).payload, expected = JsonObject("foo" := "bar"))
  }

  test("presets: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "baz"), None)
    val presets = JsonObject("foo" := "bar", "quux" := "quuz")
    val expected = JsonObject("foo" := "baz", "quux" := "quuz")
    assertEquals(obtained = event.presets(presets).payload, expected = expected)
  }
}
