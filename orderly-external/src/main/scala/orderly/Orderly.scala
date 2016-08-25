package orderly

import java.io.{File, PrintStream}

import scala.io.Source

object Orderly extends App {
  case class Config(optimize: Boolean = false, file: Option[String] = None)
  def getConfig(c: Config, args: List[String]): Config = args match {
    case x :: xs if x == "--optimize" || x == "-opt" => getConfig(c.copy(optimize = true), xs)
    case o :: f :: xs if o == "-o" && f.head != '-' => getConfig(c.copy(file = Some(f)), xs)
    case x :: xs => getConfig(c, xs)
    case Nil => c
  }


  val config = getConfig(Config(), args.toList)
  val input = if (args.length < 1) None else Some(args.last)
  val inFile = input.map(Source.fromFile).getOrElse(Source.stdin)
  val contents = inFile.getLines().mkString("\n")

  val parsed = OrderlyParser(contents) match {
    case err @ OrderlyParser.Failure(msg, _) => {
      println(err.toString)
      System.exit(1)
      Nil
    }
    case err @ OrderlyParser.Error(msg, _) => {
      println(err.toString)
      System.exit(1)
      Nil
    }
    case OrderlyParser.Success(res, _) => res
  }

  val (errs, analyzed) =  Analyzer(parsed, config.optimize)
  if (errs.nonEmpty) {
    errs.foreach(e => println(e.msg))
    System.exit(1)
  }

  val code = CodeGenerator(analyzed)

  val out =  config.file.map(f => new PrintStream(new File(f))).getOrElse(System.out)
  out.print(code)
}
