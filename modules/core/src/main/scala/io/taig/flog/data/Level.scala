package io.taig.flog.data

import cats.Show

sealed abstract class Level extends Product with Serializable

object Level {
  final case object Debug extends Level
  final case object Error extends Level
  final case object Info extends Level
  final case object Warning extends Level

  implicit val show: Show[Level] = {
    case Debug   => "debug"
    case Error   => "error"
    case Info    => "info"
    case Warning => "warning"
  }
}
