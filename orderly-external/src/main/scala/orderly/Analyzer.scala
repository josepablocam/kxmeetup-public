package orderly

import scala.util.parsing.input.Positional


object Analyzer {
  // q code to be inserted
  val LAMBDA_VALIDATOR = """{$[isUnary x;x;'y]}"""
  val DATE_WRAP = """wrapDate "D"$"""

  case class Err(s: String, loc: Positional) {
    val (line, col) = (loc.pos.line, loc.pos.column)
    def msg: String = {
      s"Found error $s at line $line and column $col"
    }
  }

  def validPrice(p: MarketOrder): Seq[Err] =
    if (p.px.v <= 0) Err("Negative price", p.px) :: Nil else Nil

  def validVolume(p: MarketOrder): Seq[Err] = {
    if (p.volume.v <= 0) Err("Negative volume", p.volume) :: Nil else Nil
  }

  // if it is a function, apply value to parse as such, if it is an identifier, just insert
  // for dates, wrap in function that checks if date matches
  def wrapLambda(m: MarketOrder): MarketOrder = m.when match {
    case Left(v @ Verbatim(c)) =>
      val lambda = if (c.trim.head == '\"') s"value $c" else c
      m.copy(when = Left(Verbatim(s"""$LAMBDA_VALIDATOR[$lambda; "${Err("Non-unary lambda", v).msg}"]""")))
    case Right(d) => m.copy(when = Left(Verbatim(s"""$DATE_WRAP "${d.str}" """)))
  }

  def collectInserts(s: Seq[SingleInsert]): Seq[BulkInsert] =
    s.groupBy(_.t).map { case (t, os) => BulkInsert(t, os.map(_.order)) }.toList

  def check(s: Seq[SingleInsert]): Seq[Err] =
     s.flatMap(i => validPrice(i.order)) ++ s.flatMap(i => validVolume(i.order))

  def optimize(s: Seq[SingleInsert]): Seq[BulkInsert] = collectInserts(s)

  def apply(s: Seq[SingleInsert], opt: Boolean = false): (Seq[Err], Seq[OrderInsert]) = {
    val errs = check(s)
    val rewritten = s.map(i => i.copy(order = wrapLambda(i.order)))
    val optimized = if (opt) optimize(rewritten) else rewritten
    (errs, optimized)
  }
}
