# Flog

> Functional logging with metadata

[![GitLab CI](https://gitlab.com/taig-github/flog/badges/master/pipeline.svg?style=flat-square)](https://gitlab.com/taig-github/flog/pipelines)
[![Maven Central](https://img.shields.io/maven-central/v/io.taig/flog-core_2.13.svg?style=flat-square)](https://search.maven.org/search?q=g:io.taig%20AND%20a:flog-*)
[![License](https://img.shields.io/github/license/taig/flog?style=flat-square)](LICENSE)

```scala
libraryDependencies ++=
  "io.taig" %% "flog-core" % "x.x.x" ::
  "io.taig" %% "flog-slf4j" % "x.x.x" ::
  Nil
```

## Usage

```scala
import cats.effect.*
import cats.effect.std.Dispatcher
import io.taig.flog.data.Level
import io.taig.flog.http4s.{CorrelationMiddleware, LoggingMiddleware}
import io.taig.flog.slf4j.FlogSlf4jBinder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.dsl.io.*
import com.comcast.ip4s.*

object SampleApp extends ResourceApp.Forever:
  def app(logger: Logger[IO]): HttpApp[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "crash" => IO.raiseError(new RuntimeException("💣"))
    case GET -> Root =>
      logger.info("I'm handling a request here, and a trace information is automagically attached to my payload!") *>
      Ok()
    }.orNotFound

  def server(logger: ContextualLogger[IO]): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(host"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(CorrelationMiddleware(logger)(LoggingMiddleware(logger)(app(logger))))
    .build

  val logger: Resource[IO, Logger[IO]] = Dispatcher
    .parallel[IO]
    .flatMap: dispatcher =>
    Resource
    .eval(Logger.stdOut[IO])
    .flatMap(Logger.queued[IO])
    .map(_.minimum(Level.Info))
    .evalTap(FlogSlf4jBinder.initialize(_, dispatcher))

  override def run(arguments: List[String]): Resource[IO, Unit] = for
    logger <- logger
    contextual <- Resource.eval(ContextualLogger.ofIO(logger))
    _ <- server(contextual)
  yield ()

```

``` 
[2023-06-17 10:08:50.582][info][server] Request
{
  "request" : {
    "method" : "GET",
    "uri" : "/",
    "headers" : {
      "Host" : "localhost:8080",
      "User-Agent" : "curl/7.88.1",
      "Accept" : "*/*"
    }
  },
  "correlation" : "351f211a-c857-4b98-a1dd-b16240fa7fa1"
}
[2023-06-17 10:08:50.597][info][/] I'm handling a request here, and a trace information is automagically attached to my payload!
{
  "correlation" : "351f211a-c857-4b98-a1dd-b16240fa7fa1"
}
[2023-06-17 10:08:50.598][info][server] Response
{
  "response" : {
    "status" : "200 OK",
    "headers" : {
      "Content-Length" : "0"
    }
  },
  "correlation" : "351f211a-c857-4b98-a1dd-b16240fa7fa1"
}
[2023-06-17 10:08:54.808][info][server] Request
{
  "request" : {
    "method" : "GET",
    "uri" : "/crash",
    "headers" : {
      "Host" : "localhost:8080",
      "User-Agent" : "curl/7.88.1",
      "Accept" : "*/*"
    }
  },
  "correlation" : "47b1dca1-50be-4c53-a621-ad16d72b1b35"
}
[2023-06-17 10:08:54.813][error][server] Request failed
{
  "correlation" : "47b1dca1-50be-4c53-a621-ad16d72b1b35"
}
java.lang.RuntimeException: 💣
	at io.taig.flog.SampleApp$$anon$1.applyOrElse(SampleApp.scala:16)
	[...]
```

## Slf4j

_Flog_ comes with its own slf4j backend in the `flog-slf4j` module. In order to enable it call `FlogSlf4jBinder.initialize` as early in your application initialization as possible.