import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val catsEffectVersion = "2.0.0"
val circeVersion = "0.12.3"
val googleApiClientVersion = "1.25.1"
val googleApiServicesSheetsVersion = "v4-rev581-1.25.0"
val googleCloudLoggingVersion = "1.98.0"
val googleOauthClientJettyVersion = "1.25.0"
val scalaCollectionCompatVersion = "2.1.2"

lazy val flog = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core.jvm, core.js, sheets, stackdriver)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %% "circe-core" % circeVersion ::
        "org.typelevel" %% "cats-effect" % catsEffectVersion ::
        Nil
  )

lazy val sheets = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.api-client" % "google-api-client" % googleApiClientVersion ::
        "com.google.apis" % "google-api-services-sheets" % googleApiServicesSheetsVersion ::
        "com.google.oauth-client" % "google-oauth-client-jetty" % googleOauthClientJettyVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion ::
        Nil
  )
  .dependsOn(core.jvm)

lazy val stackdriver = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % googleCloudLoggingVersion ::
        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion ::
        Nil
  )
  .dependsOn(core.jvm)
