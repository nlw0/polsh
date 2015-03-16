import scala.io.Source


case class PolshCpu(stack: Stream[String]=Stream(), memory: Map[String, String]=Map()) {

  def push(in: String): PolshCpu = {
    PolshCpu(in #:: stack, memory).execute
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
    println(stack.toList.reverse)
    println(memory)
    println()

    stack match {
      case s #:: _ if arithmetics.isDefinedAt(s) =>
        drop(1) run2op opArit(arithmetics(s))
      case "dup" #:: _ => drop(1) run1op {c=>op1=> c push Stream(op1, op1)}
      case "store" #:: _ => drop(1) run2op {c=>op1=>op2=> c store (op1 -> op2)}
      case "load" #:: _ => drop(1) run1op {c=>op1=> c push memory(op1)}
      case _ => this
    }
  }
  
  def opArit(f: (Int,Int)=>Int): PolshCpu=>String=>String=>PolshCpu =
    {c=>op1=>op2=> c push f(op2.toInt, op1.toInt).toString}

  val arithmetics: PartialFunction[String, (Int, Int)=>Int] = {
    case "+" => (_+_)
    case "-" => (_-_)
    case "/" => (_/_)
    case "*" => (_*_)
  }

  val opsMemo = Set("store", "fetch")

  // def run2op(f: PolshCpu=>String=>String=>PolshCpu): PolshCpu =
  //   stack match {
  //     case op1 #:: op2 #:: _ => f(drop(2))(op1)(op2)
  //     case _ => this
  //   }

  def run2op(f: PolshCpu=>String=>String=>PolshCpu): PolshCpu =
    stack match {
      // case op1 #:: op2 #:: _ => f(drop(2))(op1)(op2)
      case op1 #:: _ => drop(1) run1op {c=>op2=> f(c)(op1)(op2) }
      case _ => this
    }

  def run1op(f: PolshCpu=>String=>PolshCpu): PolshCpu =
    stack match {
      case op1 #:: _ => f(drop(1))(op1)
      case _ => this
    }  
}

object Polsh extends App {
  def inputTokens: Iterator[String] = Source.stdin.getLines flatMap (_.split(" "))

  val qq = PolshCpu()
  println(qq.push(inputTokens.toStream))
}

Polsh.main(args)
