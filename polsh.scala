import scala.io.Source
import scala.language.implicitConversions

trait PolshOperation
case class Op2Arg(f: String=>String=>PolshCpu=>PolshCpu) extends PolshOperation
case class Op1Arg(f: String=>PolshCpu=>PolshCpu) extends PolshOperation
case class Op0Arg(f: PolshCpu=>PolshCpu) extends PolshOperation

case class OpSeq(l: Stream[String])

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
      case op #:: _ if memory contains op => memory(op) match {
        case OpSeq(sub) =>
          drop(1).push(for (s <- sub.toStream) yield s)
        case _ => this
      }
      case _ => this
    }
  } else {
    stack match {
      case label #:: "]" #:: _ => drop(2).gulpTokens(Stream[String](), label)
      case _ => this
    }
  }

  def runOp(op: PolshOperation): PolshCpu = (op, stack) match {
    case (Op2Arg(f), arg #:: _) => drop(1) runOp f(arg)
    case (Op1Arg(f), arg #:: _) => drop(1) runOp f(arg)
    case (Op0Arg(f), _) => f(this)
    case _ => this
  }

  def opArith(f: (Long,Long)=>Long) = Op2Arg(op1=>op2=>c=> c push f(op2.toLong, op1.toLong).toString)

  val arith: PartialFunction[String,(Long,Long)=>Long] = {
    case "+" => (_ + _)
    case "-" => (_ - _)
    case "/" => (_ / _)
    case "*" => (_ * _)
  }

  def setGulpMode = PolshCpu(stack, memory, gulpMode=true)

  def gulpTokens(acc: Stream[String], label: String): PolshCpu = stack match {
    case "[" #:: ss => PolshCpu(ss, memory + (label -> OpSeq(acc)))
    case s #:: ss => PolshCpu(ss, memory).gulpTokens(s #:: acc, label)
  }

}

object Polsh extends App {
  def inputTokens: Iterator[String] = Source.stdin.getLines flatMap (_.split(" "))

  val qq = PolshCpu()

  println(qq.push(inputTokens.toStream))
}

Polsh.main(args)
