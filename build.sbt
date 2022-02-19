import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Version = new {
  val CatsEffect = "3.3.5"
  val CatsMtl = "1.2.1"
  val Circe = "0.14.1"
  val Fs2 = "3.2.4"
  val GoogleApiServicesLogging = "v2-rev20220204-1.32.1"
  val GoogleAuthLibraryOauth2Http = "1.4.0"
  val GoogleCloudLogging = "3.6.4"
  val Http4s = "1.0.0-M30"
  val Munit = "0.7.29"
  val MunitCatsEffect = "1.0.7"
  val Scala212 = "2.12.15"
  val Scala213 = "2.13.8"
  val Scala3 = "3.1.1"
  val ScalaCollectionCompat = "2.6.0"
  val Slf4j = "1.7.36"
  val Slugify = "2.5"
}

// Don't publish root / aggregation project
noPublishSettings

ThisBuild / crossScalaVersions := Seq(Version.Scala212, Version.Scala213, Version.Scala3)
ThisBuild / scalaVersion := Version.Scala213
ThisBuild / versionScheme := Some("early-semver")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/core"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "co.fs2" %%% "fs2-core" % Version.Fs2 ::
        "org.scala-lang.modules" %%% "scala-collection-compat" % Version.ScalaCollectionCompat ::
        "org.typelevel" %%% "cats-effect" % Version.CatsEffect ::
        "org.typelevel" %%% "cats-mtl" % Version.CatsMtl ::
        "org.scalameta" %%% "munit" % Version.Munit % "test" ::
        "org.typelevel" %%% "munit-cats-effect-3" % Version.MunitCatsEffect % "test" ::
        Nil,
    name := "flog-core"
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )

lazy val circe = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/circe"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %%% "circe-core" % Version.Circe ::
        Nil,
    name := "flog-circe"
  )
  .dependsOn(core)

lazy val slf4j = project
  .in(file("modules/slf4j"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Version.Slf4j ::
        Nil,
    name := "flog-slf4j"
  )
  .dependsOn(core.jvm)

lazy val stackdriverGrpc = project
  .in(file("modules/stackdriver-grpc"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.cloud" % "google-cloud-logging" % Version.GoogleCloudLogging ::
        Nil,
    name := "flog-stackdriver-grpc"
  )
  .dependsOn(core.jvm)

lazy val stackdriverHttp = project
  .in(file("modules/stackdriver-http"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.github.slugify" % "slugify" % Version.Slugify ::
        "com.google.auth" % "google-auth-library-oauth2-http" % Version.GoogleAuthLibraryOauth2Http ::
        "com.google.apis" % "google-api-services-logging" % Version.GoogleApiServicesLogging ::
        Nil,
    name := "flog-stackdriver-http"
  )
  .dependsOn(core.jvm)

lazy val logstash = project
  .in(file("modules/logstash"))
  .settings(sonatypePublishSettings)
  .settings(
    name := "flog-logstash"
  )
  .dependsOn(core.jvm)

lazy val http4s = project
  .in(file("modules/http4s"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-core" % Version.Http4s ::
        Nil,
    name := "flog-http4s"
  )
  .dependsOn(core.jvm)

lazy val http4sClient = project
  .in(file("modules/http4s-client"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-client" % Version.Http4s ::
        Nil,
    name := "flog-http4s-client"
  )
  .dependsOn(http4s)

lazy val http4sServer = project
  .in(file("modules/http4s-server"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-server" % Version.Http4s ::
        Nil,
    name := "flog-http4s-server"
  )
  .dependsOn(http4s)

lazy val sample = project
  .in(file("modules/sample"))
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-blaze-server" % Version.Http4s ::
        Nil,
    name := "flog-sample"
  )
  .dependsOn(http4sServer, slf4j)
