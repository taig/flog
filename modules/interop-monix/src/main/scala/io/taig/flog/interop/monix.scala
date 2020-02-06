package io.taig.flog.interop

import _root_.monix.eval.{Task, TaskLocal}
import cats.Applicative
import cats.mtl.ApplicativeLocal
import io.taig.flog.algebra.{ContextualLogger, Logger}
import io.taig.flog.data.Context

object monix {
  def applicativeLocal[A](ref: TaskLocal[A]): ApplicativeLocal[Task, A] =
    new ApplicativeLocal[Task, A] {
      override def local[B](f: A => A)(fa: Task[B]): Task[B] =
        ref.bindL(ask map f)(fa)

      override def scope[B](e: A)(fa: Task[B]): Task[B] = ref.bind(e)(fa)

      override val applicative: Applicative[Task] = Applicative[Task]

      override val ask: Task[A] = ref.read

      override def reader[B](f: A => B): Task[B] = ask.map(f)
    }

  def contextualMonixLogger(logger: Logger[Task]): Task[ContextualLogger[Task]] =
    TaskLocal(Context.Empty).map { ref =>
      implicit val F: ApplicativeLocal[Task, Context] = applicativeLocal(ref)
      ContextualLogger(logger)
    }
}
