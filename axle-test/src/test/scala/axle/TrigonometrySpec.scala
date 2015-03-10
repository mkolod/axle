package axle

import org.specs2.mutable.Specification

import axle.jung.JungDirectedGraph
import axle.jung.JungDirectedGraph.directedGraphJung
import axle.quanta.Angle
import axle.quanta.conversionGraph
import axle.quanta.UnittedQuantity
import spire.implicits.DoubleAlgebra

class TrigonometrySpec extends Specification {

  "sine(angle)" should {

    "work" in {

      implicit val amd = Angle.metadata[Double]
      //implicit val angleCG = conversionGraph[Angle, Double, JungDirectedGraph]

      axle.sine(UnittedQuantity(2d, Angle.metadata[Double].radian)) must be equalTo math.sin(2d)

    }
  }
}