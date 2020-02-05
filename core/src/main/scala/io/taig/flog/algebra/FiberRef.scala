package io.taig.flog.algebra

abstract class FiberRef[F[_], A] {
  def get: F[A]

  def set(value: A): F[Unit]

  def locally[B](value: A)(use: F[B]): F[B]
}
