package io.taig.flog.util

import io.taig.flog.data.Payload
import io.taig.flog.syntax._
import munit.FunSuite

final class JsonPrinterTest extends FunSuite {
  test("Value") {
    assertEquals(obtained = JsonPrinter(Payload.Value("foobar")), expected = "\"foobar\"")
  }

  test("Object (empty)") {
    assertEquals(obtained = JsonPrinter(Payload.Empty), expected = "{}")
  }

  test("Object (1)") {
    assertEquals(obtained = JsonPrinter(Payload.of("foo" := "bar")), expected = """{"foo":"bar"}""")
  }

  test("Object (2)") {
    assertEquals(
      obtained = JsonPrinter(Payload.of("foo" := "bar", "fiz" := "buz")),
      expected = """{"foo":"bar","fiz":"buz"}"""
    )
  }

  test("Object (nested)") {
    assertEquals(
      obtained = JsonPrinter(Payload.of("foo" := Payload.of("lorem" := "ipsum"), "bar" := Payload.Empty)),
      expected = """{"foo":{"lorem":"ipsum"},"bar":{}}"""
    )
  }
}
