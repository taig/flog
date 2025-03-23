import io.circe.Json
import io.circe.syntax._

object GithubActionsGenerator {
  object Step {
    def setupJava(version: String): Json = Json.obj(
      "name" := "Setup Java",
      "uses" := "actions/setup-java@v4",
      "with" := Json.obj(
        "distribution" := "temurin",
        "java-version" := version,
        "cache" := "sbt"
      )
    )

    val setupSbt: Json = Json.obj(
      "name" := "Setup sbt",
      "uses" := "sbt/setup-sbt@v1"
    )

    val Checkout: Json = Json.obj(
      "name" := "Checkout",
      "uses" := "actions/checkout@v4",
      "with" := Json.obj(
        "fetch-depth" := 0
      )
    )
  }

  object Job {
    def apply(name: String, mode: String = "DEV", needs: List[String] = Nil)(jobs: Json*): Json = Json.obj(
      "name" := name,
      "runs-on" := "ubuntu-latest",
      "needs" := needs,
      "env" := Json.obj(
        s"SBT_TPOLECAT_$mode" := "true"
      ),
      "steps" := jobs
    )

    def blowout(javaVersion: String): Json = Job(name = "Blowout")(
      Step.Checkout,
      Step.setupJava(javaVersion),
      Step.setupSbt,
      Json.obj("run" := "sbt blowoutCheck")
    )

    def scalafmt(javaVersion: String): Json = Job(name = "Scalafmt")(
      Step.Checkout,
      Step.setupJava(javaVersion),
      Step.setupSbt,
      Json.obj("run" := "sbt scalafmtCheckAll")
    )

    def scalafix(javaVersion: String): Json = Job(name = "Scalafix", mode = "CI")(
      Step.Checkout,
      Step.setupJava(javaVersion),
      Step.setupSbt,
      Json.obj("run" := "sbt scalafixCheckAll")
    )

    def test(javaVersion: String): Json = Job(name = "Test")(
      Step.Checkout,
      Step.setupJava(javaVersion),
      Step.setupSbt,
      Json.obj("run" := "sbt test")
    )

    def deploy(javaVersion: String): Json =
      Job(name = "Deploy", needs = List("blowout", "scalafmt", "scalafix", "test"))(
        Step.Checkout,
        Step.setupJava(javaVersion),
        Step.setupSbt,
        Json.obj(
          "name" := "Release",
          "run" := "sbt ci-release",
          "env" := Json.obj(
            "PGP_PASSPHRASE" := "${{secrets.PGP_PASSPHRASE}}",
            "PGP_SECRET" := "${{secrets.PGP_SECRET}}",
            "SONATYPE_PASSWORD" := "${{secrets.SONATYPE_PASSWORD}}",
            "SONATYPE_USERNAME" := "${{secrets.SONATYPE_USERNAME}}"
          )
        )
      )
  }

  def main(javaVersion: String): Json = Json.obj(
    "name" := "CI",
    "on" := Json.obj(
      "push" := Json.obj(
        "branches" := List("main")
      )
    ),
    "jobs" := Json.obj(
      "blowout" := Job.blowout(javaVersion),
      "scalafmt" := Job.scalafmt(javaVersion),
      "scalafix" := Job.scalafix(javaVersion),
      "test" := Job.test(javaVersion),
      "deploy" := Job.deploy(javaVersion)
    )
  )

  def tag(javaVersion: String): Json = Json.obj(
    "name" := "CD",
    "on" := Json.obj(
      "push" := Json.obj(
        "tags" := List("*.*.*")
      )
    ),
    "jobs" := Json.obj(
      "blowout" := Job.blowout(javaVersion),
      "scalafmt" := Job.scalafmt(javaVersion),
      "scalafix" := Job.scalafix(javaVersion),
      "test" := Job.test(javaVersion),
      "deploy" := Job.deploy(javaVersion)
    )
  )

  def pullRequest(javaVersion: String): Json = Json.obj(
    "name" := "CI",
    "on" := Json.obj(
      "pull_request" := Json.obj(
        "branches" := List("main")
      )
    ),
    "jobs" := Json.obj(
      "blowout" := Job.blowout(javaVersion),
      "scalafmt" := Job.scalafmt(javaVersion),
      "scalafix" := Job.scalafix(javaVersion),
      "test" := Job.test(javaVersion)
    )
  )
}
