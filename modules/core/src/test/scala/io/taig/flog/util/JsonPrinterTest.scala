package io.taig.flog.util

import io.taig.flog.data.Payload
import io.taig.flog.syntax._
import munit.FunSuite

final class JsonPrinterTest extends FunSuite {
  test("compact (Value)") {
    assertEquals(obtained = JsonPrinter.compact(Payload.Value("foobar")), expected = "\"foobar\"")
  }

  test("compact (empty Object)") {
    assertEquals(obtained = JsonPrinter.compact(Payload.Empty), expected = "{}")
  }

  test("compact (Object [1])") {
    assertEquals(obtained = JsonPrinter.compact(Payload.of("foo" := "bar")), expected = """{"foo":"bar"}""")
  }

  test("compact (Object [2])") {
    assertEquals(
      obtained = JsonPrinter.compact(Payload.of("foo" := "bar", "fiz" := "buz")),
      expected = """{"foo":"bar","fiz":"buz"}"""
    )
  }

  test("compact (Object [nested])") {
    assertEquals(
      obtained = JsonPrinter.compact(Payload.of("foo" := Payload.of("lorem" := "ipsum"), "bar" := Payload.Empty)),
      expected = """{"foo":{"lorem":"ipsum"},"bar":{}}"""
    )
  }

  test("pretty (Value)") {
    assertEquals(obtained = JsonPrinter.pretty(Payload.Value("foobar")), expected = "\"foobar\"")
  }

  test("pretty (empty Object)") {
    assertEquals(obtained = JsonPrinter.pretty(Payload.Empty), expected = "{}")
  }

  test("pretty (Object [1])") {
    val expected =
      """{
        |  "foo": "bar"
        |}""".stripMargin
    assertEquals(obtained = JsonPrinter.pretty(Payload.of("foo" := "bar")), expected)
  }

  test("pretty (Object [2])") {
    val expected =
      """{
        |  "foo": "bar",
        |  "fiz": "buz"
        |}""".stripMargin
    assertEquals(obtained = JsonPrinter.pretty(Payload.of("foo" := "bar", "fiz" := "buz")), expected)
  }

  test("pretty (Object [nested])") {
    val expected =
      """{
        |  "foo": {
        |    "lorem": "ipsum"
        |  },
        |  "bar": {}
        |}""".stripMargin
    assertEquals(
      obtained = JsonPrinter.pretty(Payload.of("foo" := Payload.of("lorem" := "ipsum"), "bar" := Payload.Empty)),
      expected
    )
  }
}
