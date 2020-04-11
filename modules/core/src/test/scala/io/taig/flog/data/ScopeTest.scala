package io.taig.flog.data

import io.taig.testf._
import io.taig.flog.dsl._

@AutoTest
object ScopeTest extends IOAutoTestApp {
  test("fromName")(
    test("class") {
      isEqual(Scope.Root / "io" / "taig" / "flog" / "data" / "Scope")(Scope.fromName[Scope])
    },
    test("object") {
      isEqual(Scope.Root / "io" / "taig" / "flog" / "data" / "Scope")(Scope.fromName[Scope.type])
    }
  )

  test("fromSimpleName")(
    test("class") {
      isEqual(Scope.Root / "Scope")(Scope.fromSimpleName[Scope])
    },
    test("object") {
      isEqual(Scope.Root / "Scope")(Scope.fromSimpleName[Scope.type])
    }
  )
}
