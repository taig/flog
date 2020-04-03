package io.taig.flog.interop

import _root_.zio.{FiberRef, Task}
import _root_.zio.interop.catz._
import cats.Applicative
import cats.mtl.ApplicativeLocal
import io.taig.flog.{ContextualLogger, Logger}
import io.taig.flog.data.Context

object zio {
  def applicativeLocal[A](ref: FiberRef[A]): ApplicativeLocal[Task, A] =
    new ApplicativeLocal[Task, A] {
      override def local[B](f: A => A)(fa: Task[B]): Task[B] =
        ref.get.flatMap(a => ref.locally(f(a))(fa))

      override def scope[B](e: A)(fa: Task[B]): Task[B] = ref.locally(e)(fa)

      override val applicative: Applicative[Task] = Applicative[Task]

      override val ask: Task[A] = ref.get

      override def reader[B](f: A => B): Task[B] = ask.map(f)
    }

  def contextualZioLogger(logger: Logger[Task]): Task[ContextualLogger[Task]] =
    FiberRef.make(Context.Empty).map { ref =>
      implicit val F: ApplicativeLocal[Task, Context] = applicativeLocal(ref)
      ContextualLogger(logger)
    }
}
