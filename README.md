# Flog

> Functional logging with metadata

[![GitLab CI](https://gitlab.com/taig-github/flog/badges/master/build.svg?style=flat-square)](https://gitlab.com/taig-github/flog/pipelines)
[![Maven Central](https://img.shields.io/maven-central/v/io.taig/flog-core_2.13.svg?style=flat-square)](https://search.maven.org/search?q=g:io.taig%20AND%20a:flog-*)
[![License](https://img.shields.io/github/license/taig/flog?style=flat-square)](LICENSE)

```scala
libraryDependencies ++=
  "io.taig" %% "flog-core" % "x.x.x" ::
  "io.taig" %% "flog-interop-zio" % "x.x.x" ::
  "io.taig" %% "flog-interop-monix" % "x.x.x" ::
  "io.taig" %% "flog-sheets" % "x.x.x" ::
  "io.taig" %% "flog-stackdriver" % "x.x.x" ::
  "io.taig" %% "flog-slf4j" % "x.x.x" ::
  Nil
```

The `core` and `interop` modules are also available for Scala.js.

```scala
libraryDependencies ++=
  "io.taig" %%% "flog-core" % "x.x.x" ::
  "io.taig" %%% "flog-interop-zio" % "x.x.x" ::
  "io.taig" %%% "flog-interop-monix" % "x.x.x" ::
  Nil
```

## Usage

```scala
import java.util.UUID

import cats.effect.ExitCode
import cats.implicits._
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import io.taig.flog.data.Scope
import io.taig.flog.interop.monix._
import monix.eval._

import scala.io.Source

object Playground extends TaskApp {
  def loadWebsite(url: String, logger: Logger[Task]): Task[String] =
    for {
      _ <- logger.info(
        Scope.Root / "request",
        message = url
      )
      body <- Task(Source.fromURL(url))
        .bracket(source => Task(source.mkString))(source => Task(source.close())
        )
      _ <- logger.info(
        Scope.Root / "response",
        message = url,
        payload = JsonObject("body" -> (body.take(100) + "...").asJson)
      )
    } yield body

  def app(logger: Logger[Task]): Task[Unit] =
    (loadWebsite(url = "https://typelevel.org", logger) *>
      loadWebsite(url = "foobar", logger)).void
      .onErrorHandleWith { throwable =>
        logger.error(message = "Execution failed", throwable = throwable.some)
      }

  override def run(args: List[String]): Task[ExitCode] =
    (for {
      // Pick a simple std out logger ...
      stdOutLogger <- Logger.stdOut[Task]
      // ... and lift it into contextual mode (which is only possible with
      // monix.eval.Task and ZIO)
      contextualLogger <- contextualMonixLogger(stdOutLogger)
      uuid <- Task(UUID.randomUUID())
      _ <- contextualLogger.locally(_.trace(uuid))(app(contextualLogger))
    } yield ExitCode.Success).executeWithOptions(_.enableLocalContextPropagation)
}
```

``` 
[2020-02-05 18:57:13.504][info][request] https://typelevel.org
{
  "trace" : "95cf9ca5-9e10-4451-9d61-111f8850ca0a"
}
[2020-02-05 18:57:13.659][info][response] https://typelevel.org
{
  "trace" : "95cf9ca5-9e10-4451-9d61-111f8850ca0a",
  "body" : "<!DOCTYPE html>\n<html>\n  <head>\n    <meta charset=\"utf-8\">\n    <meta http-equiv=\"X-UA-Compatible\" co..."
}
[2020-02-05 18:57:13.659][info][request] foobar
{
  "trace" : "95cf9ca5-9e10-4451-9d61-111f8850ca0a"
}
[2020-02-05 18:57:13.661][error][/] Execution failed
{
  "trace" : "95cf9ca5-9e10-4451-9d61-111f8850ca0a"
}
java.net.MalformedURLException: no protocol: foobar
	at java.net.URL.<init>(URL.java:610)
	at java.net.URL.<init>(URL.java:507)
	at java.net.URL.<init>(URL.java:456)
	at scala.io.Source$.fromURL(Source.scala:132)
	[...]
```

## Logstash

Basic configuration to receive message from the `LogstashLogger`

```
filter {
  json {
	  source => "message"
	}

	date {
		match => [ "timestamp", "UNIX_MS" ]
		target => "@timestamp"
	}

	mutate {
    remove_field => [ "timestamp" ]
  }
}
```

## Slf4j

_Flog_ comes with its own slf4j backend in the `flog-slf4j` module. In order to enable it call `FlogSlf4jBinder.initialize` as early in your application initialization as possible.