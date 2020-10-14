package io.taig.flog

import cats.implicits._
import cats.mtl.Local
import cats.{~>, Applicative, FlatMap}
import io.taig.flog.data.{Context, Event}
import io.taig.flog.internal.Builders

abstract class ContextualLogger[F[_]] extends Logger[F] { self =>
  def log(context: Context, events: Long => List[Event]): F[Unit]

  final def imapK[G[_]](fk: F ~> G)(gk: G ~> F): ContextualLogger[G] =
    new ContextualLogger[G] {
      override def log(context: Context, events: Long => List[Event]): G[Unit] = fk(self.log(context, events))

      override val context: G[Context] = fk(self.context)

      override def locally[A](run: G[A])(f: Context => Context): G[A] =
        fk(self.locally(gk(run))(f))

      override def scope[A](run: G[A])(context: Context): G[A] = fk(self.scope(gk(run))(context))

      override def log(events: Long => List[Event]): G[Unit] = fk(self.log(events))
    }

  def context: F[Context]

  def locally[A](run: F[A])(f: Context => Context): F[A]

  def scope[A](run: F[A])(context: Context): F[A]
}

object ContextualLogger extends Builders[ContextualLogger] {
  def apply[F[_]: FlatMap](logger: Logger[F])(implicit F: Local[F, Context]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(events: Long => List[Event]): F[Unit] = context.flatMap(log(_, events))

      final override def log(context: Context, events: Long => List[Event]): F[Unit] =
        logger.log { timestamp => events(timestamp).map(_.defaults(context)) }

      final override val context: F[Context] = F.ask

      final override def locally[A](run: F[A])(f: Context => Context): F[A] = F.local(run)(f)

      final override def scope[A](run: F[A])(context: Context): F[A] = F.scope(run)(context)
    }

  def build[F[_]](logger: ContextualLogger[F])(f: List[Event] => List[Event]): ContextualLogger[F] =
    new ContextualLogger[F] {
      def apply(events: Long => List[Event]): Long => List[Event] = timestamp => f(events(timestamp))

      override def log(context: Context, events: Long => List[Event]): F[Unit] = logger.log(context, apply(events))

      override val context: F[Context] = logger.context

      override def locally[A](run: F[A])(f: Context => Context): F[A] = logger.locally(run)(f)

      override def scope[A](run: F[A])(context: Context): F[A] = logger.scope(run)(context)

      override def log(events: Long => List[Event]): F[Unit] = logger.log(apply(events))
    }

  def noop[F[_]](implicit F: Applicative[F]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(context: Context, events: Long => List[Event]): F[Unit] = F.unit

      override def context: F[Context] = F.pure(Context.Empty)

      override def locally[A](run: F[A])(f: Context => Context): F[A] = run

      override def scope[A](run: F[A])(context: Context): F[A] = run

      override def log(events: Long => List[Event]): F[Unit] = F.unit
    }

  override def filter[F[_]](filter: Event => Boolean)(logger: ContextualLogger[F]): ContextualLogger[F] =
    build(logger)(_.filter(filter))

  override def defaults[F[_]](context: Context)(logger: ContextualLogger[F]): ContextualLogger[F] =
    build(logger)(_.map(_.defaults(context)))

  def liftNoop[F[_]: Applicative](logger: Logger[F]): ContextualLogger[F] = new ContextualLogger[F] {
    override def log(context: Context, events: Long => List[Event]): F[Unit] = logger.log(events)

    override val context: F[Context] = Context.Empty.pure[F]

    override def locally[A](run: F[A])(f: Context => Context): F[A] = run

    override def scope[A](run: F[A])(context: Context): F[A] = run

    override def log(events: Long => List[Event]): F[Unit] = logger.log(events)
  }
}
