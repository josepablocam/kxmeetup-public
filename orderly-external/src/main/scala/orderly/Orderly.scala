package orderly

import java.io.{File, PrintStream}

import scala.io.Source

object Orderly extends App {

  abstract class Action
  case object Parse extends Action
  case object Analyze extends Action
  case object Generate extends Action
  case class Config(optimize: Boolean = false, file: Option[String] = None, action: Action = Generate)
  def getConfig(c: Config, args: List[String]): Config = args match {
    case x :: xs if x == "--optimize" || x == "-opt" => getConfig(c.copy(optimize = true), xs)
    case o :: f :: xs if o == "-o" && f.head != '-' => getConfig(c.copy(file = Some(f)), xs)
    case "--parse" :: xs => getConfig(c.copy(action = Parse), xs)
    case "--analyze" :: xs => getConfig(c.copy(action = Analyze), xs)
    case "--generate" :: xs => getConfig(c.copy(action = Generate), xs)
    case x :: xs => getConfig(c, xs)
    case Nil => c
  }


  val config = getConfig(Config(), args.toList)
  val input = if (args.length < 1 || args.last.startsWith("-")) None else Some(args.last)
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

  if (config.action == Parse) {
    parsed.foreach(println)
    System.exit(0)
  }

  val (errs, analyzed) =  Analyzer(parsed, config.optimize)
  if (errs.nonEmpty) {
    errs.foreach(e => println(e.msg))
    System.exit(1)
  }

  if (config.action == Analyze) {
    analyzed.foreach(println)
    System.exit(0)
  }

  val code = CodeGenerator(analyzed)

  val out =  config.file.map(f => new PrintStream(new File(f))).getOrElse(System.out)
  if (config.action == Generate) {
    out.print(code)
    System.exit(0)
  }
}
