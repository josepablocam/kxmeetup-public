package orderly

import scala.util.parsing.input.Positional

abstract class Side
case object Buy extends Side
case object Sell extends Side

case class Verbatim(str: String) extends Positional
// wrap on normal string to provide error messages with location later on
case class PString(str: String) extends Positional

abstract class Volume extends Positional
case class NumShares(v: Int) extends Volume
case class USDAmount(v: Double) extends Volume

case class MarketOrder(
  side: Side,
  sym: String,
  shares: Double,
  px: Double,
  when: Either[Verbatim, PString],
  client: Option[String]) extends Positional

case class BulkInsert(t: String, orders: Seq[MarketOrder]) extends Positional