package io.taig.flog

import cats.Show

sealed trait Level extends Product with Serializable

object Level {
  final case object Debug extends Level
  final case object Error extends Level
  final case object Failure extends Level
  final case object Info extends Level
  final case object Warning extends Level

  implicit val show: Show[Level] = {
    case Debug   => "debug"
    case Error   => "error"
    case Failure => "failure"
    case Info    => "info"
    case Warning => "warning"
  }
}
