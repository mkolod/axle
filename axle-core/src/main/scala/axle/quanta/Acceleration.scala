package axle.quanta

import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import axle.algebra.Scale
import spire.algebra.Eq
import spire.algebra.Field

case class Acceleration() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Degree_(angle)"

}

trait AccelerationUnits extends QuantumUnits[Acceleration] {

  lazy val mpsps = unit("mps", "mps") // derive

  lazy val fpsps = unit("fps", "fps") // derive

  lazy val g = unit("g", "g", Some("http://en.wikipedia.org/wiki/Standard_gravity"))

  def units: List[UnitOfMeasurement[Acceleration]] =
    List(mpsps, fpsps, g)

}

trait AccelerationConverter[N] extends UnitConverter[Acceleration, N] with AccelerationUnits

object Acceleration {

  import spire.algebra.Module
  import spire.math._
  import spire.implicits._

  def converterGraph[N: Field: Eq, DG[_, _]: DirectedGraph](
    implicit module: Module[N, Double]) =
    new UnitConverterGraph[Acceleration, N, DG] with AccelerationConverter[N] {

      def links: Seq[(UnitOfMeasurement[Acceleration], UnitOfMeasurement[Acceleration], Bijection[N, N])] =
        List[(UnitOfMeasurement[Acceleration], UnitOfMeasurement[Acceleration], Bijection[N, N])](
          (mpsps, g, Scale(9.80665)))
    }

}
