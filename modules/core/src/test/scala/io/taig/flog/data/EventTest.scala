package io.taig.flog.data

import io.circe.Json
import io.circe.syntax._
import io.taig.flog.dsl._
import io.taig.testf.{AutoTest, IOAutoTestApp}

@AutoTest
object EventTest extends IOAutoTestApp {
  test("prefix")(
    test("both empty") {
      val event = Event(0, Level.Info, Scope.Root, "", Json.Null, None)
      isEqual(Scope.Root)(event.prefix(Scope.Root).scope)
    },
    test("prefix empty") {
      val event = Event(0, Level.Info, Scope.Root / "foobar", "", Json.Null, None)
      isEqual(Scope.Root / "foobar")(event.prefix(Scope.Root).scope)
    },
    test("event empty") {
      val event = Event(0, Level.Info, Scope.Root, "", Json.Null, None)
      isEqual(Scope.Root / "foobar")(event.prefix(Scope.Root / "foobar").scope)
    },
    test("both non-empty") {
      val event = Event(0, Level.Info, Scope.Root / "foobar", "", Json.Null, None)
      isEqual(Scope.Root / "foo" / "bar" / "foobar")(event.prefix(Scope.Root / "foo" / "bar").scope)
    }
  )

  test("presets")(
    test("both empty") {
      val event = Event(0, Level.Info, Scope.Root, "", Json.Null, None)
      isEqual(Json.Null)(event.presets(Json.Null).payload)
    },
    test("presets empty") {
      val event = Event(0, Level.Info, Scope.Root, "", Json.obj("foo" := "bar"), None)
      isEqual(Json.obj("foo" := "bar"))(event.presets(Json.Null).payload)
    },
    test("event empty") {
      skip {
        val event = Event(0, Level.Info, Scope.Root, "", Json.Null, None)
        isEqual(Json.obj("foo" := "bar"))(event.presets(Json.obj("foo" := "bar")).payload)
      }
    },
    test("both non-empty") {
      val event = Event(0, Level.Info, Scope.Root, "", Json.obj("foo" := "baz"), None)
      val presets = Json.obj("foo" := "bar", "quux" := "quuz")
      val expected = Json.obj("foo" := "baz", "quux" := "quuz")
      isEqual(expected)(event.presets(presets).payload)
    }
  )
}
