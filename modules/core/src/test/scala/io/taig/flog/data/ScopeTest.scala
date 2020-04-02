package io.taig.flog.data

import io.taig.testf._
import io.taig.flog.dsl._

@AutoTest
object ScopeTest extends IOAutoTestApp {
  test("fromClassName")(
    test("class") {
      isEqual(Scope.Root / "io" / "taig" / "flog" / "data" / "Scope")(Scope.fromClassName[Scope])
    },
    test("object") {
      isEqual(Scope.Root / "io" / "taig" / "flog" / "data" / "Scope")(Scope.fromClassName[Scope.type])
    }
  )
}
