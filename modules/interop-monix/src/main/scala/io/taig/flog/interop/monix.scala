package io.taig.flog.interop

import _root_.monix.eval.{Task, TaskLocal}
import cats.Applicative
import cats.mtl.Local
import io.taig.flog.data.Context
import io.taig.flog.{ContextualLogger, Logger}

object monix {
  def local[A](ref: TaskLocal[A]): Local[Task, A] = new Local[Task, A] {
    override def local[B](fa: Task[B])(f: A => A): Task[B] = ref.bindL(ask map f)(fa)

    override def applicative: Applicative[Task] = Applicative[Task]

    override def ask[E2 >: A]: Task[E2] = ref.read
  }

  def contextualMonixLogger(logger: Logger[Task]): Task[ContextualLogger[Task]] = TaskLocal(Context.Empty).map { ref =>
    implicit val F: Local[Task, Context] = local(ref)
    ContextualLogger(logger)
  }
}
