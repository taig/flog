# Changelog

## 0.7.2

_2020-07-31_

 * Add native-image configuration for `stackdriver-http` module
 * Upgrade to scala 2.12.12
 * Add `MonitoredResources` helper

## 0.7.1

_2020-07-24_

 * Add `StackdriverHttpLogger` error handling
 * Improve `StackdriverGrpcLogger` failureEntry payload construction

## 0.7.0

_2020-07-24_

 * Rename `stackdriver` module to `stackdriver-grpc` and introduce `stackdriver-http`
 * Upgrade to zio 1.0.0-RC21-2
 * Upgrade to zio-interop-cats 2.1.4.0-RC17
 * Upgrade to sbt-scalajs 1.1.1
 * Upgrade to cats-effect 2.1.4
 * Upgrade to sbt 1.3.13

## 0.6.12

_2020-07-01_

 * Log failures in request execution
 * Remove `filter` parameter from `LoggingMiddleware`, use `Logger.filter` instead
 * Rename `internal` package to `util`
 * Remove explicit root / aggregation module
 * Upgrade to google-cloud-logging 1.101.2

## 0.6.11

_2020-06-29_

 * Downgrade to google-cloud-logging 1.101.1, as version 1.101.2 appears to be flawed

## 0.6.10

_2020-06-29_

 * Add `StackdriverLogger.fromOptions` builder
 * Upgrade to http4s 0.21.6
 * Upgrade to scala 2.13.3
 * Upgrade to sbt-houserules 0.2.3

## 0.6.9

_2020-06-24_

 * Upgrade to sbt-houserules 0.2.2
 * Upgrade to sbt-scalajs 1.1.0
 * Upgrade to cats-effect 2.1.3
 * Upgrade to scala-collection-compat 2.1.6
 * Upgrade to fs2 2.4.2
 * Upgrade to http4s 0.21.4
 * Upgrade to google-cloud-logging 1.101.2
 * Upgrade to monix 3.2.2
 * Upgrade to scala 2.13.2
 * Upgrade to sbt 1.3.12

## 0.6.8

_2020-04-15_

 * Split `LoggingMiddleware` into `Logging`- and `TracingMiddleware`
 * Log request body when the request is canceled
 * Adjust `Client` logging structure to `Server` logging
 * Upgrade to sbt 1.3.10

## 0.6.7

_2020-04-13_

 * Add `ContextualLogger.liftNoop`
 * Use `.runAsync().unsafeRunSync()` in favor of `.toIO.unsafeRunSync()`

## 0.6.6

_2020-04-11_

 * Add `filter` parameter to `TracingMiddleware`
 * Remove ability to log request / response bodies
 
## 0.6.5

_2020-04-11_

 * Stackdriver requires a name as the logger name may not be empty
 
## 0.6.4

_2020-04-11_

 * Rename `Scope.fromClassName` to `Scope.fromName`
 * Add `Scope.fromSimpleName`
 * Always attach an `insertId` to Stackdriver LogEntries
 * Filter out Stackdriver payload `null` values
 * Use `Scope` as Stackdriver logger name
 * Remove Stackdriver global default builder

## 0.6.3

_2020-04-09_

`0.6.2` could not be released due to a misconfiguration, use ` 0.6.3` instead.

## 0.6.2

_2020-04-09_

 * Add defensive error handling for `StackdriverLogger` failures
 * Return Slf4j `NOPLoggerFactory` instead of throwing an exception when not initialized
 * Move `Logger` & `ContextualLogger` to `io.taif.flog`
 * Add `ContextualLogger.imapK`
 * Upgrade to google-cloud-logging 1.101.1

## 0.6.1

_2020-04-02_

 `0.6.0` could not be released due to a misconfiguration, use ` 0.6.1` instead.
 
  * Upgrade to scala 2.12.11
  * Fix tests compile error on Scala 2.12
  * Upgrade to http4s 0.21.3

## 0.6.0

_2020-04-02_

 * Increase Logging API surface to simplify usage
 * Revert switching from `JsonObject` to `Json`
 * Migrate tests to testf
 * Introduce `Builder.defaults to` consistently implement `prefix` & `preset`
 * Use `Event.defaults` in `ContextualLogger.apply`
 * Add `Logger.presets` and extend test suite
 * Upgrade to http4s 0.21.2

## 0.5.0

_2020-04-01_

 * Use simple `Json` values for payloads instead of `JsonObject`
 * Upgrade to google-cloud-logging 1.101.0
 * Upgrade to sbt-houserules 0.2.1
 * Upgrade to sbt 1.3.9

## 0.4.1

_2020-03-22_

 * Change Builders parameter order to improve function composition
 * Logger.stdOut to return an F rather than a Resource

## 0.4.0

_2020-03-22_

 * Add support for filters (#3)
 * Add a slf4j backend (#3)
 * Upgrade to fs2 2.3.0

## 0.3.2

_2020-03-19_

 * Fix erroneous libraryDependencies not loading scalajs
 * Add `http4s-server` module
 * Add `http4s-client` module
 * Upgrade to cats-mtl 0.7.1
 * Upgrade to zio-interop-cats 2.0.0.0-RC12
 * Upgrade to zio 1.0.0-RC18-2
 * Upgrade to cats-effect 2.1.2

## 0.3.1

_2020-02-28_

 * Add `ContextualLogger.noop`
 * Add `Logger.noTimestamp` and simplify `Slf4jLogger`
 * Document logstash config
 * Upgrade to scalatest 3.1.1

## 0.3.0

_2020-02-13_

 * Add `logstash` module
 * Add `fs2-core` dependency to `core` module
 * Improve resource handling of `Logger` builders
 * Add `Logger.queued` builder that handles events asynchronously and guarantees thread safety

## 0.2.1

_2020-02-08_

 * Upgrade to cats-effect 2.1.1
 * Upgrade to circe 0.13.0

## 0.2.0

_2020-02-06_

 * Switch from custom `FiberRef` with `HasFiberRef` typeclass to `cats-mtl.ApplicativeLocal`

## 0.1.0

_2020-02-05_

 * Switch to zio/monix `FiberRef` for contextual logging (#2)

## 0.0.5

_2019-12-21_

 * Bring back support for native timestamps
 * Improve CI integration

## 0.0.4

_2019-12-13_

 * Add basic slf4j module
 * Add `Logger.mapK`
 * Add `Logger#append` helpers
 * Upgrade to sbt-scalajs 0.6.31
 * Upgrade to google-cloud-logging 1.99.0
 * Upgrade to scala-collection-compat 2.1.3
 * Upgrade to sbt 1.3.5

## 0.0.3

_2019-10-25_

 * Simplify logger by removing state, and weaken `TracedFailure` accordingly
 * Upgrade to circe 0.12.3

## 0.0.2

_2019-10-19_

 * Add scalajs support (#1)
 * Upgrade to google-cloud-logging 1.98.0
 * Upgrade to sbt 1.3.3
 * Upgrade to sbt-houserules 0.1.1
 * Upgrade to circe 0.12.2

## 0.0.1

_2019-09-20_

 * Initial release
