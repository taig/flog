package io.taig

package object flog {
  def \\(segment: String): Scope = Scope(segment)
}
