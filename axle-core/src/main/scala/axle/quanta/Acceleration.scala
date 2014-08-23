package axle.quanta

import spire.algebra._
import spire.math._
import spire.implicits._
import axle.graph._

abstract class Acceleration[N: Field: Order: Eq] extends Quantum[N] {
  
  class AccelerationQuantity(
    magnitude: N = field.one,
    _unit: Option[Q] = None,
    _name: Option[String] = None,
    _symbol: Option[String] = None,
    _link: Option[String] = None) extends Quantity(magnitude, _unit, _name, _symbol, _link)

  type Q = AccelerationQuantity

  implicit def eqTypeclass: Eq[Q] = new Eq[Q] {
    def eqv(x: Q, y: Q): Boolean =
      (x.magnitude === y.magnitude) &&
        ((x.unitOption.isDefined && y.unitOption.isDefined && (x.unitOption.get === y.unitOption.get)) ||
            (x.unitOption.isEmpty && y.unitOption.isEmpty && x.equals(y)))
  }

  def newUnitOfMeasurement(
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): AccelerationQuantity =
    new AccelerationQuantity(field.one, None, name, symbol, link)

  def newQuantity(magnitude: N, unit: AccelerationQuantity): AccelerationQuantity =
    new AccelerationQuantity(magnitude, Some(unit), None, None, None)

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Acceleration"

}

object Acceleration extends Acceleration[Rational] {
  
  import Speed.{ mps, fps }
  import Time.{ second }

  lazy val _conversionGraph = conversions(
    List(
      derive(mps.over[Time.type, this.type](second, this)),
      derive(fps.over[Time.type, this.type](second, this)),
      unit("g", "g", Some("http://en.wikipedia.org/wiki/Standard_gravity"))
    ),
    (vs: Seq[Vertex[AccelerationQuantity]]) => vs match {
      case mpsps :: fpsps :: g :: Nil => trips2fns(List(
        (mpsps, g, 9.80665)
      ))
      case _ => Nil
    }
  )

  lazy val mpsps = byName("mpsps")
  lazy val fpsps = byName("fpsps")
  lazy val g = byName("g")

  def conversionGraph: DirectedGraph[Q, Rational => Rational] = _conversionGraph
  
}
