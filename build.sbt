import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Version = new {
  val CatsEffect = "2.3.3"
  val CatsMtl = "1.1.2"
  val Circe = "0.13.0"
  val Fs2 = "2.5.3"
  val GoogleApiServicesLogging = "v2-rev20210312-1.31.0"
  val GoogleApiServicesSheets = "v4-rev20210309-1.31.0"
  val GoogleAuthLibraryOauth2Http = "0.25.2"
  val GoogleCloudLogging = "2.2.0"
  val Http4s = "0.21.20"
  val Monix = "3.3.0"
  val Munit = "0.7.22"
  val MunitCatsEffect = "0.13.1"
  val Scala = "2.13.5"
  val ScalaCollectionCompat = "2.4.2"
  val Slf4j = "1.7.30"
  val ZioInteropCats = "2.3.1.0"
  val Zio = "1.0.5"
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
        "org.typelevel" %%% "munit-cats-effect-2" % Version.MunitCatsEffect % "test" ::
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

lazy val zio = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/interop-zio"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "dev.zio" %%% "zio-interop-cats" % Version.ZioInteropCats ::
        "dev.zio" %%% "zio" % Version.Zio ::
        Nil,
    name := "flog-interop-zio"
  )
  .dependsOn(core)

lazy val monix = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/interop-monix"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.monix" %%% "monix" % Version.Monix ::
        Nil,
    name := "flog-interop-monix"
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

lazy val sheets = project
  .in(file("modules/sheets"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.apis" % "google-api-services-sheets" % Version.GoogleApiServicesSheets ::
        "com.google.auth" % "google-auth-library-oauth2-http" % Version.GoogleAuthLibraryOauth2Http ::
        Nil,
    name := "flog-sheets"
  )
  .dependsOn(core.jvm)

lazy val stackdriverGrpc = project
  .in(file("modules/stackdriver-grpc"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
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
  .dependsOn(http4sServer, zio.jvm, slf4j)
