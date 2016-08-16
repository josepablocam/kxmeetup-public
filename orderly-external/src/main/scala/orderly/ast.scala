package orderly

import scala.util.parsing.input.Positional

abstract class Side
case object Buy extends Side
case object Sell extends Side

case class Verbatim(str: String) extends Positional
// wrap on normal string to provide error messages with location later on
case class PString(str: String) extends Positional

abstract class Volume extends Positional {
  def v: Double
}
case class NumShares(v: Double) extends Volume
case class USDAmount(v: Double) extends Volume

// wrap normal double for error messages with location
case class PDouble(v: Double) extends Positional

case class MarketOrder(
  side: Side,
  sym: String,
  volume: Volume,
  px: PDouble,
  when: Either[Verbatim, PString],
  client: Option[String]) extends Positional


abstract class OrderInsert extends Positional
case class BulkInsert(t: String, orders: Seq[MarketOrder]) extends OrderInsert
case class SingleInsert(t: String, order: MarketOrder) extends OrderInsert