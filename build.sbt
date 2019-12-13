import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val CatsEffectVersion = "2.0.0"
val CatsEffectTestingVersion = "0.3.0"
val CirceVersion = "0.12.3"
val GoogleApiClientVersion = "1.25.1"
val GoogleApiServicesSheetsVersion = "v4-rev581-1.25.0"
val GoogleCloudLoggingVersion = "1.98.0"
val GoogleOauthClientJettyVersion = "1.25.0"
val ScalaCollectionCompatVersion = "2.1.3"
val ScalatestVersion = "3.1.0"
val Slf4jVersion = "1.7.29"

lazy val flog = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core.jvm, core.js, slf4j, sheets, stackdriver)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %% "circe-core" % CirceVersion ::
        "org.typelevel" %% "cats-effect" % CatsEffectVersion ::
        "com.codecommit" %% "cats-effect-testing-scalatest" % CatsEffectTestingVersion % "test" ::
        "org.scalatest" %% "scalatest" % ScalatestVersion % "test" ::
        Nil
  )

lazy val slf4j = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.slf4j" % "slf4j-api" % Slf4jVersion ::
        Nil
  )
  .dependsOn(core.jvm)

lazy val sheets = project
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
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % GoogleCloudLoggingVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % ScalaCollectionCompatVersion ::
        Nil
  )
  .dependsOn(core.jvm)
