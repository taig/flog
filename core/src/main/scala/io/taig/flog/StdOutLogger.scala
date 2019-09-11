package io.taig.flog

import java.io._

import cats.effect.Sync
import cats.implicits._
import io.taig.flog.internal.Helpers

final class StdOutLogger[F[_]](writer: BufferedWriter)(implicit F: Sync[F])
    extends Logger[F] {
  override def apply(events: List[Event]): F[Unit] =
    F.unlessA(events.isEmpty) {
      F.delay {
        val builder = print(events)
        writer.write(builder.toString())
        writer.flush()
      }
    }

  def print(events: List[Event]): StringBuilder = {
    val builder = new StringBuilder()

    events.foreach { event =>
      builder
        .append('[')
        .append(Helpers.TimeFormatter.format(event.timestamp))
        .append("][")
        .append(event.level.show)
        .append("][")
        .append(event.scope.show)
        .append("] ")
        .append(event.message.value)

      val payload = event.payload.value

      if (payload.nonEmpty) {
        val data = payload
          .map {
            case (key, value) => s"  $key: $value"
          }
          .mkString("\n")
        builder.append('\n').append(data)
      }

      event.throwable.map(Helpers.print).foreach { value =>
        builder.append('\n').append(value)
      }

      builder.append('\n')
    }

    builder
  }
}

object StdOutLogger {
  def apply[F[_]](
      target: OutputStream
  )(implicit F: Sync[F]): F[Logger[F]] =
    F.delay(new BufferedWriter(new OutputStreamWriter(target), 1024))
      .map(new StdOutLogger[F](_))

  def apply[F[_]: Sync]: F[Logger[F]] = StdOutLogger(System.out)
}
