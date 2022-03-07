package io.taig.flog.logstash

import cats.effect._
import cats.syntax.all._
import io.taig.flog.Logger
import io.circe.syntax._
import io.taig.flog.data.Event

import java.io.{BufferedOutputStream, DataOutputStream}
import java.net.Socket

final class LogstashLogger[F[_]](channel: DataOutputStream)(implicit F: Sync[F]) extends Logger[F] {
  val timestamp: F[Long] = Clock[F].realTime.map(_.toMillis)

  def write(events: List[Event]): F[Unit] = F.blocking {
    events.foreach(event => channel.writeBytes(event.asJson.noSpaces + '\n'))
    channel.flush()
  }

  override def log(events: Long => List[Event]): F[Unit] =
    timestamp.flatMap(timestamp => write(events(timestamp)))
}

object LogstashLogger {
  def apply[F[_]](host: String, port: Int)(implicit F: Sync[F]): Resource[F, Logger[F]] = {
    val acquire = F.delay(new Socket(host, port))
    val release = (socket: Socket) => F.delay(socket.close())
    Resource
      .make(acquire)(release)
      .evalMap(socket => F.delay(socket.getOutputStream))
      .map { output =>
        val channel = new DataOutputStream(new BufferedOutputStream(output))
        new LogstashLogger[F](channel)
      }
  }
}
