package io.taig.flog.data

import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.dsl._
import io.taig.testf._

@AutoTest
object EventTest extends IOAutoTestApp {
  test("prefix")(
    test("both empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
      isEqual(Scope.Root)(event.prefix(Scope.Root).scope)
    },
    test("prefix empty") {
      val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
      isEqual(Scope.Root / "foobar")(event.prefix(Scope.Root).scope)
    },
    test("event empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
      isEqual(Scope.Root / "foobar")(event.prefix(Scope.Root / "foobar").scope)
    },
    test("both non-empty") {
      val event = Event(0, Level.Info, Scope.Root / "foobar", "", JsonObject.empty, None)
      isEqual(Scope.Root / "foo" / "bar" / "foobar")(event.prefix(Scope.Root / "foo" / "bar").scope)
    }
  )

  test("presets")(
    test("both empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
      isEqual(JsonObject.empty)(event.presets(JsonObject.empty).payload)
    },
    test("presets empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "bar"), None)
      isEqual(JsonObject("foo" := "bar"))(event.presets(JsonObject.empty).payload)
    },
    test("event empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject.empty, None)
      isEqual(JsonObject("foo" := "bar"))(event.presets(JsonObject("foo" := "bar")).payload)
    },
    test("both non-empty") {
      val event = Event(0, Level.Info, Scope.Root, "", JsonObject("foo" := "baz"), None)
      val presets = JsonObject("foo" := "bar", "quux" := "quuz")
      val expected = JsonObject("foo" := "baz", "quux" := "quuz")
      isEqual(expected)(event.presets(presets).payload)
    }
  )
}
