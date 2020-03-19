import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val CatsEffectVersion = "2.1.2"
val CatsMtlVersion = "0.7.0"
val CirceVersion = "0.13.0"
val GoogleApiClientVersion = "1.25.1"
val GoogleApiServicesSheetsVersion = "v4-rev581-1.25.0"
val GoogleCloudLoggingVersion = "1.100.0"
val GoogleOauthClientJettyVersion = "1.25.0"
val Http4sVersion = "0.21.1"
val Fs2Version = "2.2.2"
val MonixVersion = "3.1.0"
val ScalaCollectionCompatVersion = "2.1.4"
val ScalatestVersion = "3.1.1"
val Slf4jVersion = "1.7.30"
val ZioVersion = "1.0.0-RC18-2"
val ZioInteropCatsVersion = "2.0.0.0-RC10"

lazy val flog = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(
    core.jvm,
    core.js,
    zio.jvm,
    monix.jvm,
    monix.js,
    zio.js,
    slf4j,
    sheets,
    stackdriver,
    logstash,
    http4sClient,
    http4sServer
  )

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/core"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "co.fs2" %%% "fs2-core" % Fs2Version ::
        "io.circe" %%% "circe-core" % CirceVersion ::
        "org.typelevel" %%% "cats-effect" % CatsEffectVersion ::
        "org.typelevel" %%% "cats-mtl-core" % CatsMtlVersion ::
        "org.scalatest" %%% "scalatest" % ScalatestVersion % "test" ::
        Nil
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
    name := "interop-zio"
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
    name := "interop-monix"
  )
  .dependsOn(core)

lazy val slf4j = project
  .in(file("modules/slf4j"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Slf4jVersion ::
        Nil
  )
  .dependsOn(core.jvm)

lazy val sheets = project
  .in(file("modules/sheets"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.api-client" % "google-api-client" % GoogleApiClientVersion ::
        "com.google.apis" % "google-api-services-sheets" % GoogleApiServicesSheetsVersion ::
        "com.google.oauth-client" % "google-oauth-client-jetty" % GoogleOauthClientJettyVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % ScalaCollectionCompatVersion ::
        Nil
  )
  .dependsOn(core.jvm)

lazy val stackdriver = project
  .in(file("modules/stackdriver"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % GoogleCloudLoggingVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % ScalaCollectionCompatVersion ::
        Nil
  )
  .dependsOn(core.jvm)

lazy val logstash = project
  .in(file("modules/logstash"))
  .settings(sonatypePublishSettings)
  .dependsOn(core.jvm)

lazy val http4sClient = project
  .in(file("modules/http4s-client"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %%% "circe-parser" % CirceVersion ::
        "org.http4s" %% "http4s-blaze-client" % Http4sVersion ::
        Nil,
    name := "http4s-client"
  )
  .dependsOn(core.jvm)

lazy val http4sServer = project
  .in(file("modules/http4s-server"))
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion ::
        Nil,
    name := "http4s-server"
  )
  .dependsOn(core.jvm)
