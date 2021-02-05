package io.taig.flog.interop

import _root_.zio.{FiberRef, Task}
import _root_.zio.interop.catz._
import cats.Applicative
import cats.mtl.Local
import io.taig.flog.{ContextualLogger, Logger}
import io.taig.flog.data.Context

object zio {
  def local[A](ref: FiberRef[A]): Local[Task, A] =
    new Local[Task, A] {
      override def local[B](fa: Task[B])(f: A => A): Task[B] =
        ref.get.flatMap(a => ref.locally(f(a))(fa))

      override val applicative: Applicative[Task] = Applicative[Task]

      override def ask[E2 >: A]: Task[E2] = ref.get
    }

  def contextualZioLogger(logger: Logger[Task]): Task[ContextualLogger[Task]] =
    FiberRef.make(Context.Empty).map { ref =>
      implicit val F: Local[Task, Context] = local(ref)
      ContextualLogger(logger)
    }
}
