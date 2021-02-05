package io.taig.flog.data

import munit.FunSuite
import io.taig.flog.syntax._
import cats.syntax.all._

final class PayloadTest extends FunSuite {
  test("flatten (empty)") {
    assertEquals(obtained = Payload.Empty.flatten, expected = Map.empty[String, String])
  }

  test("flatten (1)") {
    assertEquals(obtained = Payload.of("foo" := "bar").flatten, expected = Map("foo" -> "bar"))
  }

  test("flatten (2)") {
    assertEquals(
      obtained = Payload.of("foo" := "bar", "lorem" := "ipsum").flatten,
      expected = Map("foo" -> "bar", "lorem" -> "ipsum")
    )
  }

  test("flatten (null)") {
    assertEquals(obtained = Payload.of("foo" := "bar", "lorem" := none[String]).flatten, expected = Map("foo" -> "bar"))
  }

  test("flatten (nested)") {
    val payload = Payload.of(
      "foo" := Payload.of("bar" := Payload.of("baz" := "1"), "lorem" := "2"),
      "ipsum" := "3"
    )
    val expected = Map(
      "foo.bar.baz" -> "1",
      "foo.lorem" -> "2",
      "ipsum" -> "3"
    )
    assertEquals(obtained = payload.flatten, expected)
  }
}
