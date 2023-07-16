package io.taig.flog.data

import munit.FunSuite

final class ScopeTest extends FunSuite:
  test("fromClassName (class)"):
    assertEquals(
      obtained = Scope.fromClassName[Level],
      expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Level"
    )

  test("fromClassName (object)"):
    assertEquals(
      obtained = Scope.fromClassName[Level.type],
      expected = Scope.Root / "io" / "taig" / "flog" / "data" / "Level"
    )

  test("fromSimpleClassName (class)"):
    assertEquals(obtained = Scope.fromSimpleClassName[Level], expected = Scope.Root / "Level")

  test("fromSimpleClassName (object)"):
    assertEquals(obtained = Scope.fromSimpleClassName[Level.type], expected = Scope.Root / "Level")
