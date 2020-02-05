package io.taig.flog.algebra

import cats.FlatMap
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import io.taig.flog.HasFiberRef
import io.taig.flog.data.{Context, Event}
import io.taig.flog.util.Circe

abstract class ContextualLogger[F[_]: FlatMap](logger: Logger[F])
    extends Logger[F] {
  final override def log(event: Long => Event): F[Unit] =
    context.flatMap(log(_, event))

  final def log(context: Context, event: Long => Event): F[Unit] =
    logger.log { timestamp =>
      val raw = event(timestamp)
      raw.copy(
        scope = context.prefix ++ raw.scope,
        payload = Circe.combine(context.payload, raw.payload)
      )
    }

  def context: F[Context]

  def locally[A](f: Context => Context)(run: F[A]): F[A]

  def locallyF[A](f: Context => F[Context])(run: F[A]): F[A]
}

object ContextualLogger {
  def apply[F[_]: FlatMap](
      logger: Logger[F],
      ref: FiberRef[F, Context]
  ): ContextualLogger[F] =
    new ContextualLogger[F](logger) {
      override val context: F[Context] = ref.get

      override def locally[A](f: Context => Context)(run: F[A]): F[A] =
        context.flatMap(context => ref.locally(f(context))(run))

      override def locallyF[A](f: Context => F[Context])(run: F[A]): F[A] =
        context.flatMap(context => ref.locallyF(f(context))(run))
    }

  def apply[F[_]: FlatMap: HasFiberRef](
      logger: Logger[F]
  ): F[ContextualLogger[F]] =
    HasFiberRef[F].make(Context.Empty).map(ContextualLogger[F](logger, _))

  def kleisli[F[_]: Sync](
      logger: Logger[F]
  ): ContextualLogger[Kleisli[F, Context, *]] = {
    new ContextualLogger[Kleisli[F, Context, *]](
      logger.mapK(Kleisli.liftK[F, Context])
    ) {
      override val context: Kleisli[F, Context, Context] =
        Kleisli.ask[F, Context]

      override def locally[A](
          f: Context => Context
      )(run: Kleisli[F, Context, A]): Kleisli[F, Context, A] = run.local(f)

      override def locallyF[A](
          f: Context => Kleisli[F, Context, Context]
      )(run: Kleisli[F, Context, A]): Kleisli[F, Context, A] =
        Kleisli(context => f(context).andThen(run).run(context))
    }
  }
}
