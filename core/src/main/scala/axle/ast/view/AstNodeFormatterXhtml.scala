package axle.ast.view

import axle.ast._
import collection._
import xml._

class XhtmlAstNodeFormatter(language: Language, highlight: Set[AstNode], conform: Boolean)
  extends AstNodeFormatter[List[xml.Node], mutable.ListBuffer[xml.Node]](language, highlight, conform) {

  override def result() = tokens.toList

  override val tokens = new mutable.ListBuffer[xml.Node]()

  override def toString(): String = tokens.toList.mkString("")

  // override def append(t: String) { tokens += t }

  override def accRaw(s: String): Unit = tokens.append(Text(s))

  override def accNewline(): Unit = tokens.appendAll(<br/>)

  override def accSpace(): Unit = tokens.append(Text(" "))

  override def accSpaces(): Unit = tokens.append(<span>&nbsp;&nbsp;&nbsp;</span>) // TODO

  // scala.xml.Utility.escape(word)
  override def accSpan(spanclass: String, s: String): Unit = tokens += <span class={ spanclass }>{ s }</span>

}