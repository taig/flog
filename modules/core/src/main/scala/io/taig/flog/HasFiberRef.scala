package io.taig.flog

import io.taig.flog.algebra.FiberRef
import simulacrum.typeclass

@typeclass
trait HasFiberRef[F[_]] {
  def make[A](value: A): F[FiberRef[F, A]]
}
