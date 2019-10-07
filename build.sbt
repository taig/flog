import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core.jvm, core.js, sheets, stackdriver)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "io.circe" %% "circe-core" % "0.12.1" ::
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
  .dependsOn(core.jvm)

lazy val stackdriver = project
  .settings(sonatypePublishSettings)
  .settings(
    libraryDependencies ++=
      "com.google.cloud" % "google-cloud-logging" % "1.96.0" ::
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2" ::
        Nil,
    name := "flog-stackdriver"
  )
  .dependsOn(core.jvm)
