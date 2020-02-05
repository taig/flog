package io.taig.flog.interop

import _root_.zio.{Task, FiberRef => ZioFiberRef}
import io.taig.flog.HasFiberRef
import io.taig.flog.algebra.FiberRef

object zio {
  implicit val hasFiberRefZio: HasFiberRef[Task] = new HasFiberRef[Task] {
    override def make[A](value: A): Task[FiberRef[Task, A]] =
      ZioFiberRef.make(value).map { zio =>
        new FiberRef[Task, A] {
          override val get: Task[A] = zio.get

          override def set(value: A): Task[Unit] = zio.set(value)

          override def locally[B](value: A)(use: Task[B]): Task[B] =
            zio.locally(value)(use)

          override def locallyF[B](value: Task[A])(use: Task[B]): Task[B] =
            value.flatMap(zio.locally(_)(use))
        }
      }
  }
}
