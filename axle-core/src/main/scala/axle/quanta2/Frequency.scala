package axle.quanta2

import axle.graph.DirectedGraph
import spire.math.Rational
import spire.implicits.eqOps
import spire.implicits.moduleOps
import spire.implicits.groupOps
import spire.implicits.multiplicativeGroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps

class Frequency extends Quantum {
  def wikipediaUrl = "TODO"
}

object Frequency extends Frequency {

  val second = newUnit[Frequency, Rational]
  
  implicit val cgTR: DirectedGraph[Quantity[Frequency, Rational], Rational => Rational] = ???

  implicit val mtr = modulize[Frequency, Rational]

  val minute = Rational(60) *: second
  
}
