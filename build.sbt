import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Version = new {
  val CatsEffect = "3.1.1"
  val CatsMtl = "1.2.0"
  val Circe = "0.13.0"
  val Fs2 = "3.0.2"
  val GoogleApiServicesLogging = "v2-rev20210422-1.31.0"
  val GoogleAuthLibraryOauth2Http = "0.25.5"
  val GoogleCloudLogging = "2.2.3"
  val Http4s = "1.0.0-M21"
  val Munit = "0.7.25"
  val MunitCatsEffect = "1.0.2"
  val Scala = "2.13.5"
  val ScalaCollectionCompat = "2.4.3"
  val Slf4j = "1.7.30"
  val Slugify = "2.5"
}

// Don't publish root / aggregation project
noPublishSettings

ThisBuild / crossScalaVersions := Seq("2.12.13", Version.Scala)
ThisBuild / scalaVersion := Version.Scala
ThisBuild / testFrameworks += new TestFramework("munit.Framework")
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
