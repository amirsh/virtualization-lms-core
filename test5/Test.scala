package test5

import test1._
import test2._

import java.io.PrintWriter
import java.io.FileOutputStream

trait Equal extends Base {
  def __equal(a: Rep[Any], b: Rep[Any]): Rep[Boolean]
}

trait EqualExp extends Equal with BaseExp {
  case class Equal(a: Exp[Any], b: Exp[Any]) extends Def[Boolean]
  def __equal(a: Rep[Any], b: Rep[Any]): Rep[Boolean] = Equal(a,b)
}

trait ScalaCodegenEqual extends ScalaCodegen { this: EqualExp =>
  
  override def emitNode(sym: Sym[_], rhs: Def[_], stream: PrintWriter) = rhs match {
    case Equal(a,b) =>  emitValDef(sym, "" + quote(a) + "==" + quote(b), stream)
    case _ => super.emitNode(sym, rhs, stream)
  }
}

trait JSCodegenEqual extends JSCodegen { this: EqualExp => // TODO: define a generic one
  
  override def emitNode(sym: Sym[_], rhs: Def[_], stream: PrintWriter) = rhs match {
    case Equal(a,b) =>  emitValDef(sym, "" + quote(a) + "==" + quote(b), stream)
    case _ => super.emitNode(sym, rhs, stream)
  }
}



trait Print extends Base {
  implicit def unit(s: String): Rep[String]
  def print(s: Rep[Any]): Rep[Unit]
}

trait PrintExp extends Print with BaseExp { this: Effects =>
  implicit def unit(s: String): Rep[String] = Const(s)
  case class Print(s: Rep[Any]) extends Def[Unit]
  def print(s: Rep[Any]) = reflectEffect(Print(s))
}

trait ScalaCodegenPrint extends ScalaCodegen { this: PrintExp => 
  override def emitNode(sym: Sym[_], rhs: Def[_], stream: PrintWriter) = rhs match {
    case Print(s) =>  emitValDef(sym, "println(" + quote(s) + ")", stream)
    case _ => super.emitNode(sym, rhs, stream)
  }
}

trait JSCodegenPrint extends JSCodegen { this: PrintExp =>
  // TODO: should have a function for this
  override def emitNode(sym: Sym[_], rhs: Def[_], stream: PrintWriter) = rhs match {
    case Print(s) =>  emitValDef(sym, "document.body.appendChild(document.createElement(\"div\"))"+
        ".appendChild(document.createTextNode("+quote(s)+"))", stream)
    case _ => super.emitNode(sym, rhs, stream)
  }
}



trait Dom extends Base {
  // not used yet...
  type DOMObjectInternal
  type DOMObject = Rep[DOMObjectInternal]
  def document: DOMObject
  def __ext__getElementById(s: Rep[String])
}





trait TestConditional { this: Arith with Equal with Print with Blocks =>
  
  def test(x: Rep[Double]): Rep[Double] = {
    
    print("yoyo")
    
    val z = if (x == x) {
      print("yoyo")
      print("xxx")
      print("yoyo")
      (x+4)
    } else {
      (x+6)
    }
    
    print("yyy")
    print("yoyo")
    
    z + (x + 4)
  }
  
}



object Test {
  
  def main(args: Array[String]) = {

    println("-- begin")

    new TestConditional with ArithExpOpt with EqualExp with PrintExp
    with ScalaCompile with ScalaCodegenBlockEffect
    with ScalaCodegenArith with ScalaCodegenEqual with ScalaCodegenPrint
    {
      val f = (x: Rep[Double]) => test(x)
      emitScalaSource(f, "Test", new PrintWriter(System.out))
      val g = compile(f)
      println(g(7))
    }
    

    new TestConditional with ArithExpOpt with EqualExp with PrintExp
    with JSCodegenBlockEffect
    with JSCodegenArith with JSCodegenEqual with JSCodegenPrint
    {
      val f = (x: Rep[Double]) => test(x)
      emitJSSource(f, "main", new PrintWriter(System.out))
      emitHTMLPage(() => f(7), new PrintWriter(new FileOutputStream("test5.html")))
    }


    println("-- end")
  }
}