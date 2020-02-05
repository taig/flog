package io.taig.flog.data

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

final class ScopeTest extends AnyWordSpec with Matchers {
  "fromClassName" should {
    "support classes" in {
      Scope.fromClassName[Scope] shouldBe
        Scope.Root / "io" / "taig" / "flog" / "data" / "Scope"
    }

    "support objects" in {
      Scope.fromClassName[Scope.type] shouldBe
        Scope.Root / "io" / "taig" / "flog" / "data" / "Scope"
    }
  }
}
