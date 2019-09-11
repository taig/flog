lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core, sheets)

lazy val core = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.typelevel" %% "cats-effect" % "2.0.0" ::
        "io.circe" %% "circe-core" % "0.12.1" ::
        Nil,
    name := "flog-core"
  )

lazy val sheets = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.api-client" % "google-api-client" % "1.25.1" ::
        "com.google.apis" % "google-api-services-sheets" % "v4-rev581-1.25.0" ::
        "com.google.oauth-client" % "google-oauth-client-jetty" % "1.25.0" ::
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2" ::
        Nil,
    name := "flog-sheets"
  )
  .dependsOn(core)
