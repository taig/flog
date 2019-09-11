# Flog

> Functional logging with metadata

```
"io.taig" %% "flog-core" % "x.x.x"
"io.taig" %% "flog-sheets" % "x.x.x"
"io.taig" %% "flog-stackdriver" % "x.x.x"
```

## Available loggers

- `StdOutLogger`  
  Print events to stdout
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
def run[F[_]](logger: Logger[F]): F[Unit] =
  logger.debug(
    Scope / "my" / "scope",
    message = "Running my function",
    payload = Map("meta" -> "data")
  )
```