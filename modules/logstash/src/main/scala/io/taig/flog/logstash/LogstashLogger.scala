package io.taig.flog.logstash

import java.io.{BufferedOutputStream, DataOutputStream}
import java.net.Socket
import java.util.concurrent.TimeUnit

import cats.effect._
import cats.syntax.all._
import io.taig.flog.Logger
import io.taig.flog.syntax._
import io.taig.flog.data.Event

final class LogstashLogger[F[_]: ContextShift](channel: DataOutputStream, blocker: Blocker)(implicit
    F: Sync[F],
    clock: Clock[F]
) extends Logger[F] {
  val timestamp: F[Long] = clock.realTime(TimeUnit.MILLISECONDS)

  def write(events: List[Event]): F[Unit] = F.delay {
    events.foreach(event => channel.writeBytes(event.asObject.toJson(pretty = false) + '\n'))
    channel.flush()
  }

  override def log(events: Long => List[Event]): F[Unit] =
    timestamp.flatMap(timestamp => blocker.blockOn(write(events(timestamp))))
}

object LogstashLogger {
  def apply[F[_]: ContextShift: Clock](host: String, port: Int)(implicit
      F: Sync[F]
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
