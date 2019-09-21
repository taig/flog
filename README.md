# Flog

> Functional logging with metadata

[![GitLab CI](https://gitlab.com/taig-github/flog/badges/master/build.svg?style=flat-square)](https://gitlab.com/taig-github/flog/pipelines)
[![Maven Central](https://img.shields.io/maven-central/v/io.taig/flog-core_2.13.svg?style=flat-square)](https://search.maven.org/search?q=g:io.taig%20AND%20a:flog-*)
[![License](https://img.shields.io/github/license/taig/flog?style=flat-square)](LICENSE)

```
"io.taig" %% "flog-core" % "x.x.x"
"io.taig" %% "flog-sheets" % "x.x.x"
"io.taig" %% "flog-stackdriver" % "x.x.x"
```

## Available loggers

- `WriterLogger`  
  Print events to a `Writer` (e.g. std out)
- `BatchLogger`  
  Collect events for a given interval before forwarding to another `Logger`
- `BroadcastLogger`  
  Forward events to multiple `Loggers`
- `SheetsLogger`  
  Send events to _Google Sheets_
- `StackdriverLogger`  
  Send events to _Stackdriver_ (on _Google Cloud Platform_)
- `NoopLogger`

## Usage

```scala
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.JsonObject

import scala.io.Source
import io.circe.syntax._

object Main extends IOApp {
  def loadWebsite(url: String, logger: Logger[IO]): IO[String] =
    for {
      _ <- logger.info(
        Scope.Root / "request",
        message = url
      )
      body <- IO(Source.fromURL(url).mkString)
      _ <- logger.info(
        Scope.Root / "response",
        message = url,
        payload = JsonObject("body" -> (body.take(100) + "...").asJson)
      )
    } yield body

  override def run(args: List[String]): IO[ExitCode] =
    WriterLogger.stdOut[IO].map(_.prefix(Scope.Root / "load")).flatMap { logger =>
      val tracer = Tracer.reporting(logger)
      tracer.run(loadWebsite(url = "https://typelevel.org", _)) *>
      tracer.run(loadWebsite(url = "foobar", _))
    }.attempt.as(ExitCode.Success)
}
```

``` 
[2019-09-20 10:15:48.919][info][load / request] https://typelevel.org
{
  "trace" : "1125b13f-69fc-4883-9f54-ca092a74455c"
}
[2019-09-20 10:15:49.263][info][load / response] https://typelevel.org
{
  "trace" : "1125b13f-69fc-4883-9f54-ca092a74455c",
  "body" : "<!DOCTYPE html>\n<html>\n  <head>\n    <meta charset=\"utf-8\">\n    <meta http-equiv=\"X-UA-Compatible\" co..."
}
[2019-09-20 10:15:49.264][info][load / request] foobar
{
  "trace" : "64aae8ad-0bdf-4ce9-ba8e-9d4e99842e7d"
}
[2019-09-20 10:15:49.265][failure][load] 
{
  "trace" : "64aae8ad-0bdf-4ce9-ba8e-9d4e99842e7d"
}
java.net.MalformedURLException: no protocol: foobar
	at java.net.URL.<init>(URL.java:593)
	at java.net.URL.<init>(URL.java:490)
	at java.net.URL.<init>(URL.java:439)
	at scala.io.Source$.fromURL(Source.scala:132)
	at io.taig.flog.Main$.$anonfun$loadWebsite$4(Main.scala:17)
	at cats.effect.internals.IORunLoop$.cats$effect$internals$IORunLoop$$loop(IORunLoop.scala:87)
	at cats.effect.internals.IORunLoop$RestartCallback.signal(IORunLoop.scala:355)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:376)
	at cats.effect.internals.IORunLoop$RestartCallback.apply(IORunLoop.scala:316)
	at cats.effect.internals.IOShift$Tick.run(IOShift.scala:36)
	at cats.effect.internals.PoolUtils$$anon$2$$anon$3.run(PoolUtils.scala:51)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
```