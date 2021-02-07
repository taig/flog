package io.taig.flog

import cats.mtl.Local
import cats.syntax.all._
import cats.{~>, Applicative, Monad}
import io.taig.flog.data.{Context, Event}

abstract class ContextualLogger[F[_]] extends Logger[F] { self =>
  def context: F[Context]

  def local[A](f: Context => Context)(run: F[A]): F[A]

  def scope[A](context: Context)(run: F[A]): F[A]

  final def imapK[G[_]](fk: F ~> G)(gk: G ~> F): ContextualLogger[G] = new ContextualLogger[G] {
    override def log(events: Long => List[Event]): G[Unit] = fk(self.log(events))

    override def context: G[Context] = fk(self.context)

    override def local[A](f: Context => Context)(run: G[A]): G[A] = fk(self.local(f)(gk(run)))

    override def scope[A](context: Context)(run: G[A]): G[A] = fk(self.scope(context)(gk(run)))
  }
}

object ContextualLogger {
  def apply[F[_]: Monad](logger: Logger[F])(implicit F: Local[F, Context]): ContextualLogger[F] =
    new ContextualLogger[F] {
      override def log(events: Long => List[Event]): F[Unit] =
        context.flatMap(context => logger.log(timestamp => events(timestamp).map(_.withContext(context))))

      override def context: F[Context] = F.ask

      override def local[A](f: Context => Context)(run: F[A]): F[A] = F.local(run)(f)

      override def scope[A](context: Context)(run: F[A]): F[A] = F.scope(run)(context)
    }

  def fake[F[_]: Applicative](logger: Logger[F]): ContextualLogger[F] = new ContextualLogger[F] {
    override def context: F[Context] = Context.Empty.pure[F]

    override def local[A](f: Context => Context)(run: F[A]): F[A] = run

    override def scope[A](context: Context)(run: F[A]): F[A] = run

    override def log(events: Long => List[Event]): F[Unit] = logger.log(events)
  }

  def noop[F[_]](implicit F: Applicative[F]): ContextualLogger[F] = new ContextualLogger[F] {
    override def context: F[Context] = Context.Empty.pure[F]

    override def local[A](f: Context => Context)(run: F[A]): F[A] = run

    override def scope[A](context: Context)(run: F[A]): F[A] = run

    override def log(events: Long => List[Event]): F[Unit] = F.unit
  }

  implicit class Ops[F[_]](logger: ContextualLogger[F]) extends LoggerOps[ContextualLogger, F] {
    override def modify(f: List[Event] => List[Event]): ContextualLogger[F] = new ContextualLogger[F] {
      override def log(events: Long => List[Event]): F[Unit] = logger.log(events)

      override def context: F[Context] = logger.context

      override def local[A](f: Context => Context)(run: F[A]): F[A] = logger.local(f)(run)

      override def scope[A](context: Context)(run: F[A]): F[A] = logger.scope(context)(run)
    }
  }
}
