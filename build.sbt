import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbtcrossproject.{CrossProject, Platform}
import scala.util.chaining._

val Version = new {
  val CatsEffect = "3.3.14"
  val CatsMtl = "1.3.0"
  val Circe = "0.14.3"
  val Fs2 = "3.3.0"
  val GoogleApiServicesLogging = "v2-rev20220819-2.0.0"
  val GoogleAuthLibraryOauth2Http = "1.11.0"
  val GoogleCloudLogging = "3.11.1"
  val Http4s = "1.0.0-M30"
  val Munit = "0.7.29"
  val MunitCatsEffect = "1.0.7"
  val Scala212 = "2.12.16"
  val Scala213 = "2.13.8"
  val Scala3 = "3.2.0"
  val ScalaCollectionCompat = "2.8.1"
  val Slf4j = "2.0.1"
  val Slugify = "3.0.2"
}

def module(identifier: String, platforms: Seq[Platform]): CrossProject =
  CrossProject(identifier, file(s"modules/$identifier"))(platforms: _*)
    .crossType(CrossType.Pure)
    .build()
    .settings(sonatypePublishSettings)
    .settings(
      name := s"flog-$identifier"
    )
    .pipe { project =>
      if (platforms.contains(JSPlatform))
        project.jsSettings(
          scalacOptions ++= { if (scalaVersion.value == Version.Scala3) "-scalajs" :: Nil else Nil }
        )
      else project
    }

noPublishSettings

ThisBuild / crossScalaVersions := Seq(Version.Scala212, Version.Scala213, Version.Scala3)
ThisBuild / scalaVersion := Version.Scala213
ThisBuild / versionScheme := Some("early-semver")

lazy val core = module("core", platforms = Seq(JVMPlatform, JSPlatform))
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

lazy val slf4j = module("slf4j", platforms = Seq(JVMPlatform))
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

lazy val stackdriverGrpc = module("stackdriver-grpc", platforms = Seq(JVMPlatform))
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.cloud" % "google-cloud-logging" % Version.GoogleCloudLogging ::
        Nil
  )
  .dependsOn(core)

lazy val stackdriverHttp = module("stackdriver-http", platforms = Seq(JVMPlatform))
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.auth" % "google-auth-library-oauth2-http" % Version.GoogleAuthLibraryOauth2Http ::
        "com.google.apis" % "google-api-services-logging" % Version.GoogleApiServicesLogging ::
        Nil
  )
  .dependsOn(core)

lazy val logstash = module("logstash", platforms = Seq(JVMPlatform)).dependsOn(core)

lazy val http4s = module("http4s", platforms = Seq(JVMPlatform))
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-core" % Version.Http4s ::
        Nil
  )
  .dependsOn(core)

lazy val http4sClient = module("http4s-client", platforms = Seq(JVMPlatform))
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-client" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4s)

lazy val http4sServer = module("http4s-server", platforms = Seq(JVMPlatform))
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-server" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4s)

lazy val sample = module("sample", platforms = Seq(JVMPlatform))
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-blaze-server" % Version.Http4s ::
        Nil
  )
  .dependsOn(http4sServer, slf4j)
