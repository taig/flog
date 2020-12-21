import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val CatsEffectVersion = "2.3.1"
val CatsMtlVersion = "1.1.1"
val CirceVersion = "0.13.0"
val Fs2Version = "2.4.6"
val GoogleApiClientVersion = "1.25.1"
val GoogleApiServicesLoggingVersion = "v2-rev20200619-1.30.10"
val GoogleApiServicesSheetsVersion = "v4-rev20200707-1.30.10"
val GoogleAuthLibraryOauth2HttpVersion = "0.21.1"
val GoogleCloudLoggingVersion = "1.102.0"
val Http4sVersion = "0.21.14"
val MonixVersion = "3.3.0"
val ScalaCollectionCompatVersion = "2.3.2"
val ScalatestVersion = "3.1.1"
val Slf4jVersion = "1.7.30"
val TestfVersion = "0.1.5"
val ZioInteropCatsVersion = "2.2.0.1"
val ZioVersion = "1.0.3"

// Don't publish root / aggregation project
noPublishSettings

ThisBuild / testFrameworks += new TestFramework("io.taig.testf.runner.TestF")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/core"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "co.fs2" %%% "fs2-core" % Fs2Version ::
        "io.circe" %%% "circe-core" % CirceVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % ScalaCollectionCompatVersion ::
        "org.typelevel" %%% "cats-effect" % CatsEffectVersion ::
        "org.typelevel" %%% "cats-mtl" % CatsMtlVersion ::
        "io.taig" %%% "testf-auto" % TestfVersion % "test" ::
        "io.taig" %%% "testf-runner-sbt" % TestfVersion % "test" ::
        Nil,
    name := "flog-core"
  )

lazy val zio = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/interop-zio"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "dev.zio" %%% "zio-interop-cats" % ZioInteropCatsVersion ::
        "dev.zio" %%% "zio" % ZioVersion ::
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
      "io.monix" %%% "monix" % MonixVersion ::
        Nil,
    name := "flog-interop-monix"
  )
  .dependsOn(core)

lazy val slf4j = project
  .in(file("modules/slf4j"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Slf4jVersion ::
        Nil,
    name := "flog-slf4j"
  )
  .dependsOn(core.jvm)

lazy val sheets = project
  .in(file("modules/sheets"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.apis" % "google-api-services-sheets" % GoogleApiServicesSheetsVersion ::
        "com.google.auth" % "google-auth-library-oauth2-http" % GoogleAuthLibraryOauth2HttpVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % ScalaCollectionCompatVersion ::
        Nil,
    name := "flog-sheets"
  )
  .dependsOn(core.jvm)

lazy val stackdriverGrpc = project
  .in(file("modules/stackdriver-grpc"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % GoogleCloudLoggingVersion ::
        Nil,
    name := "flog-stackdriver-grpc"
  )
  .dependsOn(core.jvm)

lazy val stackdriverHttp = project
  .in(file("modules/stackdriver-http"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.auth" % "google-auth-library-oauth2-http" % GoogleAuthLibraryOauth2HttpVersion ::
        "com.google.apis" % "google-api-services-logging" % GoogleApiServicesLoggingVersion ::
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

lazy val http4sClient = project
  .in(file("modules/http4s-client"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %%% "circe-parser" % CirceVersion ::
        "org.http4s" %% "http4s-client" % Http4sVersion ::
        Nil,
    name := "flog-http4s-client"
  )
  .dependsOn(core.jvm)

lazy val http4sServer = project
  .in(file("modules/http4s-server"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-server" % Http4sVersion ::
        Nil,
    name := "flog-http4s-server"
  )
  .dependsOn(core.jvm)
