package io.taig.flog.util

import io.taig.flog.data.Payload
import io.taig.flog.syntax._
import munit.FunSuite

final class JsonPrinterTest extends FunSuite {
  test("pretty (Value)") {
    assertEquals(obtained = JsonPrinter.pretty(Payload.Value("foobar")), expected = "\"foobar\"")
  }

  test("pretty (empty Object)") {
    assertEquals(obtained = JsonPrinter.pretty(Payload.Empty), expected = "{}")
  }

  test("pretty (Object [1])") {
    assertEquals(obtained = JsonPrinter.pretty(Payload.of("foo" := "bar")), expected = """{"foo":"bar"}""")
  }

  test("pretty (Object [2])") {
    assertEquals(
      obtained = JsonPrinter.pretty(Payload.of("foo" := "bar", "fiz" := "buz")),
      expected = """{"foo":"bar","fiz":"buz"}"""
    )
  }

  test("pretty (Object [nested])") {
    assertEquals(
      obtained = JsonPrinter.pretty(Payload.of("foo" := Payload.of("lorem" := "ipsum"), "bar" := Payload.Empty)),
      expected = """{"foo":{"lorem":"ipsum"},"bar":{}}"""
    )
  }
}
