package io.taig.flog.algebra

import cats.{Applicative, FlatMap}
import cats.implicits._
import cats.mtl.ApplicativeLocal
import io.taig.flog.data.{Context, Event}
import io.taig.flog.internal.Filters
import io.taig.flog.util.Circe

abstract class ContextualLogger[F[_]] extends Logger[F] {
  def log(context: Context, events: Long => List[Event]): F[Unit]

  def context: F[Context]

  def locally[A](f: Context => Context)(run: F[A]): F[A]

  def scope[A](context: Context)(run: F[A]): F[A]
}

object ContextualLogger extends Filters[ContextualLogger] {
  def apply[F[_]: FlatMap](
      logger: Logger[F]
  )(implicit F: ApplicativeLocal[F, Context]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(events: Long => List[Event]): F[Unit] =
        context.flatMap(log(_, events))

      final override def log(
          context: Context,
          events: Long => List[Event]
      ): F[Unit] =
        logger.log { timestamp =>
          events(timestamp).map { event =>
            event.copy(
              scope = context.prefix ++ event.scope,
              payload = Circe.combine(context.payload, event.payload)
            )
          }
        }

      final override val context: F[Context] = F.ask

      final override def locally[A](f: Context => Context)(run: F[A]): F[A] =
        F.local(f)(run)

      final override def scope[A](context: Context)(run: F[A]): F[A] =
        F.scope(context)(run)
    }

  def noop[F[_]](implicit F: Applicative[F]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(context: Context, events: Long => List[Event]): F[Unit] =
        F.unit

      override def context: F[Context] = F.pure(Context.Empty)

      override def locally[A](f: Context => Context)(run: F[A]): F[A] = run

      override def scope[A](context: Context)(run: F[A]): F[A] = run

      override def log(events: Long => List[Event]): F[Unit] = F.unit
    }

  override def filter[F[_]](
      logger: ContextualLogger[F]
  )(filter: Event => Boolean): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(context: Context, events: Long => List[Event]): F[Unit] =
        logger.log(context, timestamp => events(timestamp).filter(filter))

      override val context: F[Context] = logger.context

      override def locally[A](f: Context => Context)(run: F[A]): F[A] =
        logger.locally(f)(run)

      override def scope[A](context: Context)(run: F[A]): F[A] =
        logger.scope(context)(run)

      override def log(events: Long => List[Event]): F[Unit] =
        logger.log(timestamp => events(timestamp).filter(filter))
    }
}
