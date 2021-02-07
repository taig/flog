package io.taig.flog.data

import munit.FunSuite
import io.taig.flog.syntax._

final class EventTest extends FunSuite {
  test("prefix: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.Empty, None)
    assertEquals(obtained = event.prepend(Scope.Root).scope, expected = Scope.Root)
  }

  test("prefix: prefix empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", Payload.Empty, None)
    assertEquals(obtained = event.prepend(Scope.Root).scope, expected = Scope.Root / "foobar")
  }

  test("prefix: event empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.Empty, None)
    assertEquals(obtained = event.prepend(Scope.Root / "foobar").scope, expected = Scope.Root / "foobar")
  }

  test("prefix: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root / "foobar", "", Payload.Empty, None)
    assertEquals(
      obtained = event.prepend(Scope.Root / "foo" / "bar").scope,
      expected = Scope.Root / "foo" / "bar" / "foobar"
    )
  }

  test("presets: both empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.Empty, None)
    assertEquals(obtained = event.merge(Payload.Empty).payload, expected = Payload.Empty)
  }

  test("presets: presets empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.of("foo" := "bar"), None)
    assertEquals(obtained = event.merge(Payload.Empty).payload, expected = Payload.of("foo" := "bar"))
  }

  test("presets: event empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.Empty, None)
    assertEquals(obtained = event.merge(Payload.of("foo" := "bar")).payload, expected = Payload.of("foo" := "bar"))
  }

  test("presets: both non-empty") {
    val event = Event(0, Level.Info, Scope.Root, "", Payload.of("foo" := "baz"), None)
    val presets = Payload.of("foo" := "bar", "quux" := "quuz")
    val expected = Payload.of("foo" := "baz", "quux" := "quuz")
    assertEquals(obtained = event.merge(presets).payload, expected = expected)
  }
}
