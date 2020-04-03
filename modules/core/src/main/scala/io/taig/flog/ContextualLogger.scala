package io.taig.flog

import cats.implicits._
import cats.mtl.ApplicativeLocal
import cats.{~>, Applicative, FlatMap}
import io.taig.flog.data.{Context, Event}
import io.taig.flog.internal.Builders

abstract class ContextualLogger[F[_]] extends Logger[F] { self =>
  def log(context: Context, events: Long => List[Event]): F[Unit]

  final def imapK[G[_]](fk: F ~> G)(gk: G ~> F): ContextualLogger[G] =
    new ContextualLogger[G] {
      override def log(context: Context, events: Long => List[Event]): G[Unit] = fk(self.log(context, events))

      override val context: G[Context] = fk(self.context)

      override def locally[A](f: Context => Context)(run: G[A]): G[A] =
        fk(self.locally(f)(gk(run)))

      override def scope[A](context: Context)(run: G[A]): G[A] = fk(self.scope(context)(gk(run)))

      override def log(events: Long => List[Event]): G[Unit] = fk(self.log(events))
    }

  def context: F[Context]

  def locally[A](f: Context => Context)(run: F[A]): F[A]

  def scope[A](context: Context)(run: F[A]): F[A]
}

object ContextualLogger extends Builders[ContextualLogger] {
  def apply[F[_]: FlatMap](logger: Logger[F])(implicit F: ApplicativeLocal[F, Context]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(events: Long => List[Event]): F[Unit] = context.flatMap(log(_, events))

      final override def log(context: Context, events: Long => List[Event]): F[Unit] =
        logger.log { timestamp =>
          events(timestamp).map(_.defaults(context))
        }

      final override val context: F[Context] = F.ask

      final override def locally[A](f: Context => Context)(run: F[A]): F[A] = F.local(f)(run)

      final override def scope[A](context: Context)(run: F[A]): F[A] = F.scope(context)(run)
    }

  def build[F[_]](logger: ContextualLogger[F])(f: List[Event] => List[Event]): ContextualLogger[F] =
    new ContextualLogger[F] {
      def apply(events: Long => List[Event]): Long => List[Event] = timestamp => f(events(timestamp))

      override def log(context: Context, events: Long => List[Event]): F[Unit] = logger.log(context, apply(events))

      override val context: F[Context] = logger.context

      override def locally[A](f: Context => Context)(run: F[A]): F[A] = logger.locally(f)(run)

      override def scope[A](context: Context)(run: F[A]): F[A] = logger.scope(context)(run)

      override def log(events: Long => List[Event]): F[Unit] = logger.log(apply(events))
    }

  def noop[F[_]](implicit F: Applicative[F]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(context: Context, events: Long => List[Event]): F[Unit] = F.unit

      override def context: F[Context] = F.pure(Context.Empty)

      override def locally[A](f: Context => Context)(run: F[A]): F[A] = run

      override def scope[A](context: Context)(run: F[A]): F[A] = run

      override def log(events: Long => List[Event]): F[Unit] = F.unit
    }

  override def filter[F[_]](filter: Event => Boolean)(logger: ContextualLogger[F]): ContextualLogger[F] =
    build(logger)(_.filter(filter))

  override def defaults[F[_]](context: Context)(logger: ContextualLogger[F]): ContextualLogger[F] =
    build(logger)(_.map(_.defaults(context)))
}
