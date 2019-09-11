lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core, sheets, stackdriver)

lazy val core = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "org.typelevel" %% "cats-effect" % "2.0.0" ::
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

lazy val stackdriver = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % "1.90.0" ::
        Nil,
    name := "flog-stackdriver"
  )
  .dependsOn(core)
