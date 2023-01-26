import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbtcrossproject.{CrossProject, Platform}
import scala.util.chaining._

val Version = new {
  val CatsEffect = "3.4.5"
  val CatsMtl = "1.3.0"
  val Circe = "0.14.3"
  val Fs2 = "3.5.0"
  val GoogleApiServicesLogging = "v2-rev20230120-2.0.0"
  val GoogleAuthLibraryOauth2Http = "1.14.0"
  val GoogleCloudLogging = "3.14.2"
  val Java = "17"
  val Http4s = "1.0.0-M30"
  val Munit = "0.7.29"
  val MunitCatsEffect = "1.0.7"
  val Scala212 = "2.12.17"
  val Scala213 = "2.13.10"
  val Scala3 = "3.2.1"
  val ScalaCollectionCompat = "2.9.0"
  val Slf4j = "1.7.36"
  val Slugify = "3.0.2"
}

def module(identifier: Option[String], jvmOnly: Boolean): CrossProject = {
  val platforms = List(JVMPlatform) ++ (if (jvmOnly) Nil else List(JSPlatform))
  CrossProject(identifier.getOrElse("root"), file(identifier.fold(".")("modules/" + _)))(platforms: _*)
    .crossType(CrossType.Pure)
    .withoutSuffixFor(JVMPlatform)
    .build()
    .settings(
      name := "flog" + identifier.fold("")("-" + _),
      scalacOptions ++= (if (scalaVersion.value == Version.Scala3 && crossProjectPlatform.value == JSPlatform)
                           "-scalajs" :: Nil
                         else Nil)
    )
}

inThisBuild(
  Def.settings(
    crossScalaVersions := Seq(Version.Scala212, Version.Scala213, Version.Scala3),
    developers := List(Developer("taig", "Niklas Klein", "mail@taig.io", url("https://taig.io/"))),
    dynverVTagPrefix := false,
    homepage := Some(url("https://github.com/taig/flog/")),
    licenses := List("MIT" -> url("https://raw.githubusercontent.com/taig/flog/main/LICENSE")),
    organization := "io.taig",
    scalaVersion := Version.Scala213,
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
        Nil
    }
  )
  .aggregate(core, slf4j, stackdriverGrpc, stackdriverHttp, logstash, http4s, http4sClient, http4sServer, sample)

lazy val core = module(Some("core"), jvmOnly = false)
  .settings(
    libraryDependencies ++=
      "co.fs2" %%% "fs2-core" % Version.Fs2 ::
        "io.circe" %%% "circe-core" % Version.Circe ::
        "org.scala-lang.modules" %%% "scala-collection-compat" % Version.ScalaCollectionCompat ::
        "org.typelevel" %%% "cats-effect" % Version.CatsEffect ::
        "org.typelevel" %%% "cats-mtl" % Version.CatsMtl ::
        "org.scalameta" %%% "munit" % Version.Munit % "test" ::
        "org.typelevel" %%% "munit-cats-effect-3" % Version.MunitCatsEffect % "test" ::
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

lazy val stackdriverGrpc = module(Some("stackdriver-grpc"), jvmOnly = true)
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.cloud" % "google-cloud-logging" % Version.GoogleCloudLogging ::
        Nil
  )
  .dependsOn(core)

lazy val stackdriverHttp = module(Some("stackdriver-http"), jvmOnly = true)
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.auth" % "google-auth-library-oauth2-http" % Version.GoogleAuthLibraryOauth2Http ::
        "com.google.apis" % "google-api-services-logging" % Version.GoogleApiServicesLogging ::
        Nil
  )
  .dependsOn(core)

lazy val logstash = module(Some("logstash"), jvmOnly = true).dependsOn(core)

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
      "org.http4s" %% "http4s-blaze-server" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4sServer, slf4j)
