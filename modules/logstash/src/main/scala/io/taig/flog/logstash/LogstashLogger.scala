package io.taig.flog.logstash

import java.io.{BufferedOutputStream, DataOutputStream}
import java.net.Socket
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.implicits._
import io.circe.Printer
import io.circe.syntax._
import io.taig.flog.algebra.Logger
import io.taig.flog.data.Event

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
