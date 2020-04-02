# Changelog

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
