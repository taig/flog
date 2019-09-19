package io.taig.flog

import java.io._

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.taig.flog.internal.Shows._

final class WriterLogger[F[_]](writer: Writer)(implicit F: Sync[F])
    extends SyncLogger[F] {
  override def apply(event: Event): F[Unit] =
    F.delay {
      val builder = print(event)
      writer.write(builder.toString())
      writer.flush()
    }

  def print(event: Event): StringBuilder = {
    val builder = new StringBuilder()

    builder
      .append('[')
      .append(event.timestamp.show)
      .append("][")
      .append(event.level.show)
      .append("][")
      .append(event.scope.show)
      .append("] ")
      .append(event.message.value)

    val payload = event.payload.value

    if (payload.nonEmpty)
      builder.append('\n').append(Json.fromJsonObject(payload).spaces2)

    event.throwable.map(_.show).foreach { value =>
      builder.append('\n').append(value)
    }

    builder.append('\n')
  }
}

object WriterLogger {
  def apply[F[_]](
      target: OutputStream
  )(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), 1024))
      .map(new WriterLogger[F](_))

  def stdOut[F[_]: Sync]: F[Logger[F]] = WriterLogger(System.out)
}
