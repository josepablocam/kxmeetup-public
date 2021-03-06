package orderly

import java.text.SimpleDateFormat

import scala.language.implicitConversions
import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

object OrderlyParser extends JavaTokenParsers {
  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r

  val dateParser = new SimpleDateFormat("MM/dd/yyyy")
  dateParser.setLenient(false)

  case class Keyword(s: String) {
    lazy val parser = s | s.toUpperCase
  }

  implicit def kwtoParser(kw: Keyword): Parser[String] = kw.parser

  val BUY = Keyword("buy")
  val SELL= Keyword("sell")
  val SHARES = Keyword("shares")
  val ON = Keyword("on")
  val OF = Keyword("of")
  val AT = Keyword("at")
  val FOR = Keyword("for")
  val IF = Keyword("if")

  // orderly programs are a sequence of insertions
  def program = rep1(order ~ ("->" ~> cleanIdent) ^^ { case o ~ t => SingleInsert(t, o) })

  // market order
  def order: Parser[MarketOrder] = positioned(
    side ~ volume ~
      (OF ~> cleanIdent) ~
      (AT ~> "$" ~> floatingPointNumber) ~
      modifier ~
      (FOR ~> cleanIdent).? ^^ {
      case s ~ v ~ sym ~ px ~ m ~ c => MarketOrder(s, sym, v, PDouble(px.toDouble), m, c)
    }
    | failure ("Marked orders should specify side, volume of purchase, ticker, price, modifier and optionally a client")
    )

  def side: Parser[Side] =
   (BUY ^^ { _ => Buy }
      | SELL ^^ { _ => Sell }
      | failure("Market side should be: sell/buy")
      )

  def volume: Parser[Volume] = positioned(
    wholeNumber <~ SHARES ^^ { s => NumShares(s.toInt) }
      | "$" ~> floatingPointNumber ^^ { s => USDAmount(s.toDouble) }
      | failure("Volumes can be specified as <whole-number> shares or $<numeric>")
      )

  def modifier: Parser[Either[Verbatim, PString]] = (
    IF ~> validCode ^^ { c =>
      val wrap = Verbatim(c.str)
      wrap.setPos(c.pos)
      Left(wrap)
    }
    | ON ~> validDate ^^ { Right(_) }
    | failure("Modifiers must be identifier (lambda), quoted q code or on <date>")
    )


  def validCode: Parser[PString] = positioned(
    cleanIdent ^^ { PString }
      | stringLiteral ^^ { PString }
  )

  def validDate: Parser[PString] = positioned(
    date.filter { ps => Try(dateParser.parse(ps.str)).isSuccess }
  | failure("Invalid date provided")
  )

  def date: Parser[PString] = positioned(
    wholeNumber ~ ('/' ~> wholeNumber) ~ ('/' ~> wholeNumber) ^^ {
      case m ~ d ~ y => PString(m + "/" + d + "/" + y)
    }
      | failure("Expected date as MM/dd/yyyy")
      )

  // excludes keywords
  def keywords: Parser[String] =
    this
    .getClass
    .getMethods
    .filter(_.getReturnType == classOf[Keyword])
    .map(_.invoke(this).asInstanceOf[Keyword])
    .map(_.parser)
    .reduce(_ | _)

  def cleanIdent: Parser[String] =
    (keywords ~> err("Keywords are not identifiers")
      | ident
      | failure("Expected identifier")
      )

  def parse[A](prod: Parser[A], s: String) = parseAll(prod, s)

  def apply[A](prog: String) = parseAll(program, prog)

}