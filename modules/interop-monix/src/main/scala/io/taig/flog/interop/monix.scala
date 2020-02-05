package io.taig.flog.interop

import _root_.monix.eval.{Task, TaskLocal}
import io.taig.flog.HasFiberRef
import io.taig.flog.algebra.FiberRef

object monix {
  implicit val hasFiberRefMonix: HasFiberRef[Task] = new HasFiberRef[Task] {
    override def make[A](value: A): Task[FiberRef[Task, A]] =
      TaskLocal(value).map { ref =>
        new FiberRef[Task, A] {
          override val get: Task[A] = ref.read

          override def set(value: A): Task[Unit] = ref.write(value)

          override def locally[B](value: A)(use: Task[B]): Task[B] =
            ref.bind(value)(use)

          override def locallyF[B](value: Task[A])(use: Task[B]): Task[B] =
            ref.bindL(value)(use)
        }
      }
  }
}
