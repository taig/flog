package io.taig.flog.algebra

import cats.FlatMap
import cats.implicits._
import cats.mtl.ApplicativeLocal
import io.taig.flog.data.{Context, Event}
import io.taig.flog.util.Circe

final class ContextualLogger[F[_]: FlatMap](logger: Logger[F])(
    implicit F: ApplicativeLocal[F, Context]
) extends Logger[F] {
  override def log(event: Long => Event): F[Unit] =
    context.flatMap(log(_, event))

  def log(context: Context, event: Long => Event): F[Unit] =
    logger.log { timestamp =>
      val raw = event(timestamp)
      raw.copy(
        scope = context.prefix ++ raw.scope,
        payload = Circe.combine(context.payload, raw.payload)
      )
    }

  val context: F[Context] = F.ask

  def locally[A](f: Context => Context)(run: F[A]): F[A] = F.local(f)(run)

  def scope[A](context: Context)(run: F[A]): F[A] = F.scope(context)(run)
}

object ContextualLogger {
  def apply[F[_]: FlatMap](
      logger: Logger[F]
  )(implicit F: ApplicativeLocal[F, Context]): ContextualLogger[F] =
    new ContextualLogger[F](logger)
}
