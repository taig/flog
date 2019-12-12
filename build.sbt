import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val CatsEffectVersion = "2.0.0"
val CirceVersion = "0.12.3"
val GoogleApiClientVersion = "1.25.1"
val GoogleApiServicesSheetsVersion = "v4-rev581-1.25.0"
val GoogleCloudLoggingVersion = "1.98.0"
val GoogleOauthClientJettyVersion = "1.25.0"
val ScalaCollectionCompatVersion = "2.1.2"

lazy val flog = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core.jvm, core.js, sheets, stackdriver)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %% "circe-core" % CirceVersion ::
        "org.typelevel" %% "cats-effect" % CatsEffectVersion ::
        Nil
  )

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
