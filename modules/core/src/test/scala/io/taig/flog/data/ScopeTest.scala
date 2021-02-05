package io.taig.flog.data

import munit.FunSuite

final class ScopeTest extends FunSuite {
  test("fromName (class)") {
    assertEquals(obtained = Scope.fromName[Scope], expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Scope")
  }

  test("fromName (object)") {
    assertEquals(
      obtained = Scope.fromName[Scope.type],
      expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Scope"
    )
  }

  test("fromSimpleName (class)") {
    assertEquals(obtained = Scope.fromSimpleName[Scope], expected = Scope.Root / "Scope")
  }

  test("fromSimpleName (object)") {
    assertEquals(obtained = Scope.fromSimpleName[Scope.type], expected = Scope.Root / "Scope")
  }
}
