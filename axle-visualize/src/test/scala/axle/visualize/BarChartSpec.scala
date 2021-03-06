package axle.visualize

import org.specs2.mutable.Specification

//import spire.algebra._
//import spire.implicits._
//import axle.algebra._
import axle.algebra.DirectedGraph
import axle.quanta.Angle
import axle.jung.JungDirectedGraph
import spire.algebra.Eq
import spire.algebra.Field
import spire.math.Rational
import spire.implicits.DoubleAlgebra

class BarChartSpec extends Specification {

  "BarChart" should {
    "work" in {

      val sales = Map(
        "apple" -> 83.8,
        "banana" -> 77.9,
        "coconut" -> 10.1)

      // The Modules conflict with the Zero needed by the DataView, so we have to create the
      // angleMeta with all arguments passed explicitly.
      val angleMeta = Angle.converterGraph[Double, JungDirectedGraph](
        implicitly[Field[Double]],
        implicitly[Eq[Double]],
        implicitly[DirectedGraph[JungDirectedGraph]],
        axle.algebra.modules.doubleDoubleModule,
        axle.algebra.modules.doubleRationalModule)

      val chart = BarChart[String, Double, Map[String, Double]](
        sales,
        xAxis = 0d,
        labelAngle = 36d *: angleMeta.degree,
        title = Some("fruit sales"))

      1 must be equalTo 1
    }
  }

  "BarChartGrouped" should {
    "work" in {

      val fruits = Vector("apple", "banana", "coconut")

      val years = Vector(2011, 2012)

      val sales = Map(
        ("apple", 2011) -> 43.0,
        ("apple", 2012) -> 83.8,
        ("banana", 2011) -> 11.3,
        ("banana", 2012) -> 77.9,
        ("coconut", 2011) -> 88.0,
        ("coconut", 2012) -> 10.1)

      val chart = BarChartGrouped[String, Int, Double, Map[(String, Int), Double]](
        sales,
        xAxis = 0d,
        title = Some("fruit sales"))

      1 must be equalTo 1
    }
  }

}