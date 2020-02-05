package io.taig.flog.algebra

import java.util.UUID

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import cats.{FlatMap, Monad}
import io.circe.JsonObject
import io.circe.syntax._
import io.taig.flog.HasFiberRef
import io.taig.flog.data.{Context, Event, Scope}
import io.taig.flog.util.Circe

abstract class ContextualLogger[F[_]: FlatMap](logger: Logger[F])
    extends Logger[F] {
  override final def log(event: Long => Event): F[Unit] =
    context.flatMap { context =>
      logger.log { timestamp =>
        val raw = event(timestamp)
        raw.copy(
          scope = context.prefix ++ raw.scope,
          payload = Circe.combine(context.payload, raw.payload)
        )
      }
    }

  def context: F[Context]

  def locally[A](f: Context => Context)(run: F[A]): F[A]

  final def prefix[A](f: Scope => Scope)(run: F[A]): F[A] =
    locally(context => context.copy(prefix = f(context.prefix)))(run)

  final def payload[A](f: JsonObject => JsonObject)(run: F[A]): F[A] =
    locally(context => context.copy(payload = f(context.payload)))(run)

  final def append[A](scope: Scope)(run: F[A]): F[A] =
    prefix(_ ++ scope)(run)

  final def trace[A](run: F[A])(implicit F: Sync[F]): F[A] =
    for {
      uuid <- F.delay(UUID.randomUUID())
      result <- locally(_.combine(JsonObject("trace" := uuid)))(run)
    } yield result
}

object ContextualLogger {
  def apply[F[_]: FlatMap: HasFiberRef](
      logger: Logger[F]
  ): F[ContextualLogger[F]] =
    HasFiberRef[F].make(Context.Empty).map { ref =>
      new ContextualLogger[F](logger) {
        override val context: F[Context] = ref.get

        override def locally[A](f: Context => Context)(run: F[A]): F[A] =
          context.flatMap(context => ref.locally(f(context))(run))
      }
    }

  def kleisli[F[_]: Monad](
      logger: Logger[F]
  ): ContextualLogger[Kleisli[F, Context, *]] =
    new ContextualLogger[Kleisli[F, Context, *]](
      logger.mapK(Kleisli.liftK[F, Context])
    ) {
      override def context: Kleisli[F, Context, Context] =
        Kleisli.ask[F, Context]

      override def locally[A](
          f: Context => Context
      )(run: Kleisli[F, Context, A]): Kleisli[F, Context, A] =
        run.local(f)
    }
}
