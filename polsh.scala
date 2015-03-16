import scala.io.Source


case class PolshCpu(stack: Stream[String]=Stream(), memory: Map[String, String]=Map()) {

  def push(in: String): PolshCpu = {
    val q = PolshCpu(in #:: stack, memory)
    println(q.stack.toList.reverse)
    // println(q.memory)
    // println()
    q.execute
  }
  
  def push(in: Stream[String]): PolshCpu =
    in match {
      case tok #:: ts if (tok != "") => push(tok).push(ts)
      case _ => this
    }

  def drop(n: Int) = {
    PolshCpu(stack.drop(n), memory)
  }

  def store(kv: (String, String)) = PolshCpu(stack, memory + kv)

  def execute: PolshCpu = {
    stack match {
      case s #:: _ if opsArit.isDefinedAt(s) =>
        drop(1) runFuncArit opsArit(s)
      case s #:: _ if opsMemo.contains(s) =>
        drop(1) funcMem s
      case _ => this
    }
  }

  def runFuncArit(f: (Int,Int)=>Int) =
    stack match {
      case op2 #:: op1 #:: _ => drop(2) push f(op1.toInt, op2.toInt).toString
      case _ => this
    }

  val opsArit: PartialFunction[String, (Int, Int)=>Int] = {
    case "+" => (_+_)
    case "-" => (_-_)
    case "/" => (_/_)
    case "*" => (_*_)
  }

  val opsMemo = Set("store", "fetch")

  def funcMem(fname: String) = fname match {
    case "store" => drop(2) store (stack(0) -> stack(1))
    case "fetch" => drop(1) push memory(stack(0))
  }
  
}

object Polsh extends App {
  def inputTokens: Iterator[String] = Source.stdin.getLines flatMap (_.split(" "))

  val qq = PolshCpu()
  println(qq.push(inputTokens.toStream))
}

Polsh.main(args)
