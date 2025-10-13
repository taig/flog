import sbtcrossproject.CrossPlugin.autoImport.CrossType
import sbtcrossproject.CrossProject

val Version = new {
  val CatsEffect = "3.6.3"
  val Circe = "0.14.15"
  val Fs2 = "3.12.2"
  val Http4s = "1.0.0-M46"
  val Java = "17"
  val Log4Cats = "2.7.1"
  val Munit = "1.2.1"
  val MunitCatsEffect = "2.1.0"
  val Scala3 = "3.3.6"
  val Slf4j = "1.7.36"
  val Slf4j2 = "2.0.17"
}

def module(identifier: Option[String], jvmOnly: Boolean = false): CrossProject = {
  val platforms = List(JVMPlatform) ++ (if (jvmOnly) Nil else List(JSPlatform))
  CrossProject(identifier.getOrElse("root"), file(identifier.fold(".")("modules/" + _)))(platforms: _*)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JVMPlatform)
    .build()
    .settings(
      Compile / scalacOptions ++= "-source:future" :: "-rewrite" :: "-new-syntax" :: "-Wunused:all" :: Nil,
      name := "flog" + identifier.fold("")("-" + _)
    )
}

inThisBuild(
  Def.settings(
    developers := List(Developer("taig", "Niklas Klein", "mail@taig.io", url("https://taig.io/"))),
    dynverVTagPrefix := false,
    homepage := Some(url("https://github.com/taig/flog/")),
    licenses := List("MIT" -> url("https://raw.githubusercontent.com/taig/flog/main/LICENSE")),
    organization := "io.taig",
    scalaVersion := Version.Scala3,
    versionScheme := Some("early-semver")
  )
)

lazy val root = module(identifier = None, jvmOnly = true)
  .enablePlugins(BlowoutYamlPlugin)
  .settings(noPublishSettings)
  .settings(
    blowoutGenerators ++= {
      val github = file(".github")
      val workflows = github / "workflows"

      BlowoutYamlGenerator.lzy(workflows / "main.yml", GithubActionsGenerator.main(Version.Java)) ::
        BlowoutYamlGenerator.lzy(workflows / "pull-request.yml", GithubActionsGenerator.pullRequest(Version.Java)) ::
        BlowoutYamlGenerator.lzy(workflows / "tag.yml", GithubActionsGenerator.tag(Version.Java)) ::
        Nil
    }
  )
  .aggregate(core, slf4j, slf4j2, http4s, http4sClient, http4sServer, sample)

lazy val core = module(Some("core")).settings(
  libraryDependencies ++=
    "co.fs2" %%% "fs2-core" % Version.Fs2 ::
      "io.circe" %%% "circe-core" % Version.Circe ::
      "org.typelevel" %%% "cats-effect" % Version.CatsEffect ::
      "org.scalameta" %%% "munit" % Version.Munit % "test" ::
      "org.typelevel" %%% "munit-cats-effect" % Version.MunitCatsEffect % "test" ::
      Nil
)

lazy val slf4j = module(Some("slf4j"), jvmOnly = true)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoObject := "Build",
    buildInfoKeys := Seq("slf4jVersion" -> Version.Slf4j),
    buildInfoPackage := s"${organization.value}.flog.slf4j",
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Version.Slf4j ::
        Nil
  )
  .dependsOn(core)

lazy val slf4j2 = module(Some("slf4j-2"), jvmOnly = true)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Version.Slf4j2 ::
        Nil
  )
  .dependsOn(core)

lazy val log4cats = module(Some("log4cats"))
  .settings(
    libraryDependencies ++=
      "org.typelevel" %%% "log4cats-core" % Version.Log4Cats ::
        Nil
  )
  .dependsOn(core)

lazy val http4s = module(Some("http4s"), jvmOnly = true)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-core" % Version.Http4s ::
        Nil
  )
  .dependsOn(core)

lazy val http4sClient = module(Some("http4s-client"), jvmOnly = true)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-client" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4s)

lazy val http4sServer = module(Some("http4s-server"), jvmOnly = true)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-server" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4s)

lazy val sample = module(Some("sample"), jvmOnly = true)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-dsl" % Version.Http4s ::
        "org.http4s" %% "http4s-ember-server" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4sServer, log4cats)
