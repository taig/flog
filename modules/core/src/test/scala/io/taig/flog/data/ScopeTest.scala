package io.taig.flog.data

import munit.FunSuite

final class ScopeTest extends FunSuite {
  test("fromName (class)") {
    assertEquals(obtained = Scope.fromName[Level], expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Level")
  }

  test("fromName (object)") {
    assertEquals(
      obtained = Scope.fromName[Level.type],
      expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Level"
    )
  }

  test("fromSimpleName (class)") {
    assertEquals(obtained = Scope.fromSimpleName[Level], expected = Scope.Root / "Level")
  }

  test("fromSimpleName (object)") {
    assertEquals(obtained = Scope.fromSimpleName[Level.type], expected = Scope.Root / "Level")
  }
}
