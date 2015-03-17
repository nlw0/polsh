import scala.io.Source
import scala.language.implicitConversions

trait PolshOperation
case class Op2Arg(f: String=>String=>PolshCpu=>PolshCpu) extends PolshOperation
case class Op1Arg(f: String=>PolshCpu=>PolshCpu) extends PolshOperation
case class Op0Arg(f: PolshCpu=>PolshCpu) extends PolshOperation

case class PolshCpu(stack: Stream[String]=Stream(), memory: Map[String, Any]=Map(), gulpMode:Boolean=false) {

  implicit def iop2arg(f: String=>String=>PolshCpu=>PolshCpu) = Op2Arg(f)
  implicit def iop1arg(f: String=>PolshCpu=>PolshCpu) = Op1Arg(f)
  implicit def iop0arg(f: PolshCpu=>PolshCpu) = Op0Arg(f)


  def verbose = {println(stack.toList.reverse +" "+ memory); this}

  def push(in: String): PolshCpu =
    PolshCpu(in #:: stack, memory, gulpMode=gulpMode).verbose.execute

  def push(in: Stream[String]): PolshCpu = in match {
    case tok #:: ts if (tok != "") => push(tok).push(ts)
    case _ => this
  }

  def drop(n: Int) = PolshCpu(stack.drop(n), memory)
  def store(kv: (String, String)) = PolshCpu(stack, memory + kv)
  def free(k: String) = PolshCpu(stack, memory - k)

  def execute: PolshCpu = if (!gulpMode) {
    stack match {
      case "dup" #:: _ => drop(1) runOp Op1Arg(op1=>c=> c push Stream(op1, op1))
      case "swap" #:: _ => drop(1) runOp Op2Arg(op1=>op2=>c=> c push Stream(op1, op2))
      case "store" #:: _ => drop(1) runOp Op2Arg(op1=>op2=>c=> c store (op1 -> op2))
      case "load" #:: _ => drop(1) runOp Op1Arg(op1=>c=> c push memory(op1).toString)
      case "[" #:: _ => setGulpMode
      case op #:: _ if arith.isDefinedAt(op) => drop(1) runOp opArith(arith(op))
      case _ => this
    }
  } else {
    stack match {
      case label #:: "]" #:: _ => drop(2).gulpTokens(List[String](), label)
      case _ => this
    }
  }

  def runOp(op: PolshOperation): PolshCpu = (op, stack) match {
    case (Op2Arg(f), arg #:: _) => drop(1) runOp f(arg)
    case (Op1Arg(f), arg #:: _) => drop(1) runOp f(arg)
    case (Op0Arg(f), _) => f(this)
    case _ => this
  }

  def opArith(f: (Int,Int)=>Int) = Op2Arg(op1=>op2=>c=> c push f(op2.toInt, op1.toInt).toString)

  val arith: PartialFunction[String,(Int,Int)=>Int] = {
    case "+" => (_ + _)
    case "-" => (_ - _)
    case "/" => (_ / _)
    case "*" => (_ * _)
  }

  def setGulpMode = PolshCpu(stack, memory, gulpMode=true)

  def gulpTokens(acc: List[String], label: String): PolshCpu = stack match {
    case "[" #:: ss => PolshCpu(ss, memory + (label -> acc))
    case s #:: ss => PolshCpu(ss, memory).gulpTokens(s :: acc, label)
  }

}

object Polsh extends App {
  def inputTokens: Iterator[String] = Source.stdin.getLines flatMap (_.split(" "))

  val qq = PolshCpu()
  println(qq.push(inputTokens.toStream))
}

Polsh.main(args)
