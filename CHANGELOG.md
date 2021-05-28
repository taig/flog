# Changelog

## 0.10.8

_2021-05-28_

 * Add support for Scala 3 (#98)
 * Update http4s-blaze-server, http4s-client, ... to 1.0.0-M23 (#100)
 * Update circe-core to 0.14.1 (#99)

## 0.10.7

_2021-05-23_

 * Migrate master -> main
 * Update http4s to 1.0.0-M22 (#97)
 * Update cats-effect to 3.1.1 (#85)
 * Update fs2 to 3.0.4 (#96)
 * Update sbt-houserules to 0.3.10 (#94)
 * Update scala to 2.13.6 (#93)
 * Update google-cloud-logging to 2.2.3 (#83)
 * Update google-auth-library-oauth2-http to 0.26.0 (#95)
 * Update google-api-services-logging to v2-rev20210507-1.31.0 (#91)
 * Update munit to 0.7.26 (#84)
 * Update munit-cats-effect-3 to 1.0.3 (#88)
 * Update cats-mtl to 1.2.1 (#86)
 * Update scala-collection-compat to 2.4.4 (#87)
 * Update sbt to 1.5.2 (#82)

## 0.10.6

_2021-04-28_

 * Fix slf4j StaticMDCAdapter initialization
 * Update cats-effect to 3.1.0 (#73)
 * Update cats-mtl to 1.2.0 (#72)
 * Update fs2-core to 3.0.2 (#76)
 * Update sbt to 1.5.1 (#77)
 * Update google-cloud-logging to 2.2.2 (#79)
 * Update google-api-services-logging to v2-rev20210416-1.31.0 (#78)
 * Update google-auth-library-oauth2-http to 0.25.5 (#75)
 * Update munit-cats-effect-3 to 1.0.2 (#74)
 * Update munit to 0.7.25 (#70)

## 0.10.5

_2021-04-14_

 * Apply slugify to gcloud grpc logger name

## 0.10.4

_2021-04-14_

 * Catch dispatcher already shutdown exception in slf4j logger
 * Dump log messages that fail to be handled in slf4j bridge to stderr
 * Remove FlogSlf4JBinder.initialize method that creates its own dispatcher
 * Upgrade slugify to 2.5 (#68)

## 0.10.3

_2021-04-13_

 * Revisit slf4j bridge, mostly copied by zio-logging
 * Use slugify library to encode stackdriver (http) logName

## 0.10.2

_2021-04-13_

 * Include erroneous log entries in stackdriver error handling
 * Update google-auth-library-oauth2-http to 0.25.3 (#67)

## 0.10.1

_2021-04-11_

 * Upgrade to http4s 1.0.0-M21
 * Update cats-effect to 3.0.2 (#66)
 * Update google-cloud-logging to 2.2.1 (#64)
 * Update sbt to 1.5.0 (#63)
 * Update google-api-services-logging to v2-rev20210325-1.31.0 (#62)
 * Update sbt-scalajs, scalajs-compiler, ... to 1.5.1 (#61)
 * Update fs2-core to 3.0.1 (#59)
 * Update munit-cats-effect-3 to 1.0.1 (#60)

## 0.10.0

_2021-03-30_

 * Remove `sheets`, `zio` and `monix` modules
 * Upgrade to cats-effect 3.0.1 (#58)
 * Update sbt to 1.5.0-RC2 (#47)

## 0.9.6

_2021-03-22_

 * [#12] Add batched logger (#16)
 * A lot of scala steward

## 0.9.5

_2021-02-07_

 * Align `StackdriverGrpcLogger` API with `StackdriverHttpLogger`
 * Add `grpc.MonitoredResources` helper
 * Fix Stackdriver configuration issues

## 0.9.4

_2021-02-07_

 * Swap argument order in `ContextualLogger.local` and `.scope`
 * Streamline data and `LoggerOps` APIs

## 0.9.3

_2021-02-06_

 * Rename `Trace` to `Correlation`
 * Use `Chain` instead of `List` for `Scope`

## 0.9.2

_2021-02-06_

 * Change LoggingMiddleware to use Http, not HttpRoutes
 * Change TracingMiddleware to use Http, not HttpRoutes
 * Add some convenience methods

## 0.9.1

_2021-02-06_

 * Add `toJson` conversion methods to `circe` module
 * Properly escape json

## 0.9.0

_2021-02-05_

 * Remove circe from core (#11)

## 0.8.1

_2021-02-05_

 * [#4] Queued logger doesn't flush remaining items before shutdown
 * Replace import cats.implicits._ with cats.syntax.all._
 * Migrate tests to munit
 * Reorganize dependency versions
 * Update http4s-client, http4s-server to 0.21.18 (#10)
 * Update zio to 1.0.4-2 (#9)
 * Update google-api-services-logging to v2-rev20201114-1.31.0 (#5)
 * Update google-api-services-sheets to v4-rev20201130-1.31.0 (#6)
 * Upgrade to sbt 1.4.7
 * Upgrade to google-auth-library-oauth2-http 0.23.0
 * Upgrade to google-cloud-logging 2.1.3
 * Upgrade to sbt-scalajs 1.4.0
 * Upgrade to scala-collection-compat 2.4.1
 * Upgrade to fs2 2.5.0
 * Upgrade to monix 3.3.0
 * Upgrade to cats-mtl 1.1.1
 * Upgrade to cats-effect 2.3.1
 * Upgrade to scala 2.13.4
 * Upgrade to sbt-houserules 0.3.2
 * Migrate to gitlab CI caching mechanism

## 0.8.0

_2020-10-14_

 * Upgrade to cats-mtl 1.0.0
 * Upgrade to sbt-houserules 0.3.0

## 0.7.5

_2020-10-11_

 * http4s client should not depend on blaze-client, but just client
 * http4s server should not depend on blaze-server, but just server
 * Upgrade to zio 1.0.3
 * Upgrade to http4s 0.21.7
 * Upgrade to cats-effect 2.2.0
 * Upgrade to google-cloud-logging 1.102.0
 * Upgrade to scala-collection-compat 2.2.0
 * Upgrade to sbt-scalajs 1.2.0
 * Upgrade to fs2 2.4.4
 * Upgrade to zio 1.0.1
 * Upgrade to sbt 1.4.0

## 0.7.4

_2020-08-08_

 * Upgrade sheets logger dependencies
 * Upgrade to zio 1.0.0
 * Add Scope.from(Simple)Name(value: Any) methods

## 0.7.3

_2020-08-02_

 * Fix logName in failureEntry

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
