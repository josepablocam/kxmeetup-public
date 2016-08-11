package orderly

import scala.io.Source

object Orderly extends App {
  val input = if (args.length < 1) None else Some(args(0))
  val inFile = input.map(Source.fromFile).getOrElse(Source.stdin)
  val contents = inFile.getLines().mkString("\n")
  OrderlyParser(contents) match {
    case err @ OrderlyParser.Failure(msg, _) => println(err.toString)
    case err @ OrderlyParser.Error(msg, _) => println(err.toString)
    case OrderlyParser.Success(res, _) => println(res)
  }
}
