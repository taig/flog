//package io.taig.flog
//
//import io.taig.flog.algebra.{ContextualLogger, Logger}
//import zio.{Task, ZIO}
//import zio.interop.catz._
//import zio.interop.catz.implicits._
//
//object Playground extends zio.App {
//  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
//    Logger.stdOut[Task].flatMap(ContextualLogger[Task]).flatMap { logger =>
//      logger.traced {
//        logger.info(message = "Hello World! (:") *>
//        logger.info(message = "Hello Moon! (:") *>
//        logger.info(message = "Hello Sun! (:") *>
//        logger.info(message = "Hello Pluto! (:")
//      }
//    }.fold(_ => 1, _ => 0)
//}
