# Flog

> Functional logging with metadata

[![GitLab CI](https://gitlab.com/taig-github/flog/badges/master/build.svg?style=flat-square)](https://gitlab.com/taig-github/flog/pipelines)
[![Maven Central](https://img.shields.io/maven-central/v/io.taig/flog_2.13.svg?style=flat-square)](https://index.scala-lang.org/taig/flog)

```
"io.taig" %% "flog-core" % "x.x.x"
"io.taig" %% "flog-sheets" % "x.x.x"
"io.taig" %% "flog-stackdriver" % "x.x.x"
```

## Available loggers

- `WriterLogger`  
  Print events to a `Writer` (e.g. std out)
- `ScopedLoogger`  
  Prefix events with an additional `Scope`
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
val logger: Logger[IO] = StdOutLogger[IO].unsafeRunSync()

logger.debug(
  Scope.Root / "my" / "scope",
  message = "Running my function",
  payload = Map("meta" -> "data")
).unsafeRunSync()
```