package io.taig.flog.data

import cats.syntax.all.*
import cats.{Order, Show}
import io.circe.Encoder

enum Level:
  case Debug
  case Error
  case Info
  case Warning

object Level:
  given Order[Level] = Order.by:
    case Debug   => 0
    case Info    => 1
    case Warning => 2
    case Error   => 3

  given Show[Level] =
    case Debug   => "debug"
    case Error   => "error"
    case Info    => "info"
    case Warning => "warning"

  given Encoder[Level] = Encoder[String].contramap(_.show)
