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
}
