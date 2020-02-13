package io.taig.flog.logstash

import io.taig.flog.algebra.Logger
import io.taig.flog.data.{Event, Scope}
import java.io.{BufferedOutputStream, DataOutputStream}
import java.net.Socket
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.implicits._
import io.circe.{JsonObject, Printer}
import io.circe.syntax._

final class LogstashLogger[F[_]: ContextShift](
    channel: DataOutputStream,
    blocker: Blocker
)(
    implicit F: Sync[F],
    clock: Clock[F]
) extends Logger[F] {
  val timestamp: F[Long] = clock.realTime(TimeUnit.MILLISECONDS)

  val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  def write(event: Event): F[Unit] = F.delay {
    channel.writeBytes(printer.print(event.asJson) + '\n')
    channel.flush()
  }

  override def log(event: Long => Event): F[Unit] =
    timestamp.flatMap(timestamp => blocker.blockOn(write(event(timestamp))))
}

object LogstashLogger {
  def apply[F[_]: ContextShift: Clock](
      host: String,
      port: Int,
      blocker: Blocker
  )(
      implicit F: Sync[F]
  ): Resource[F, Logger[F]] = {
    val acquire = F.delay(new Socket(host, port))
    val release = (socket: Socket) => F.delay(socket.close())
    Resource
      .make(acquire)(release)
      .evalMap(socket => F.delay(socket.getOutputStream))
      .map { output =>
        val channel = new DataOutputStream(new BufferedOutputStream(output))
        new LogstashLogger[F](channel, blocker)
      }
  }
}

object App extends IOApp {
  import scala.concurrent.duration._
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      blocker <- Blocker[IO]
      logger <- LogstashLogger[IO]("localhost", 5000, blocker).flatMap(
        Logger.queued[IO](_)
      )
    } yield logger).use { logger =>
      logger.info(
        scope = Scope.Root / "foo" / "bar",
        message = "hello world",
        payload = JsonObject(
          "yolo" := "swag",
          "foo" := JsonObject(
            "bar" := 42
          )
        ),
        throwable = Some(new RuntimeException("This did not go well"))
      ) *>
        IO.sleep(3.seconds).as(ExitCode.Success)
    }
}
