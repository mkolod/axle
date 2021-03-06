package axle.quanta

import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import axle.algebra.Scale10s
import axle.algebra.BijectiveIdentity
import spire.algebra.Eq
import spire.algebra.Field

case class Mass() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Orders_of_magnitude_(mass)"
  // "http://en.wikipedia.org/wiki/Mass"

}

trait MassUnits extends QuantumUnits[Mass] {

  lazy val gram = unit("gram", "g")
  lazy val tonne = unit("tonne", "T", Some("http://en.wikipedia.org/wiki/Tonne"))
  lazy val milligram = unit("milligram", "mg")
  lazy val kilogram = unit("kilogram", "Kg")
  lazy val megagram = unit("megagram", "Mg")
  lazy val kilotonne = unit("kilotonne", "KT")
  lazy val megatonne = unit("megatonne", "MT")
  lazy val gigatonne = unit("gigatonne", "GT")
  lazy val teratonne = unit("teratonne", "TT")
  lazy val petatonne = unit("petatonne", "PT")
  lazy val exatonne = unit("exatonne", "ET")
  lazy val zettatonne = unit("zettatonne", "ZT")
  lazy val yottatonne = unit("yottatonne", "YT")

  //
  //  // TODO hydrogen atom
  //
  //  // 10^24 kg = ^21 t = ^12 gt = ^9 tt = ^6 pt = ^3 et = ^0 zt

  def units: List[UnitOfMeasurement[Mass]] =
    List(gram, tonne, milligram, kilogram, megagram, kilotonne, megatonne, gigatonne, teratonne,
      petatonne, exatonne, zettatonne, yottatonne)

}

trait MassConverter[N] extends UnitConverter[Mass, N] with MassUnits

object Mass {

  def converterGraph[N: Field: Eq, DG[_, _]: DirectedGraph] =
    new UnitConverterGraph[Mass, N, DG] with MassConverter[N] {

      def links: Seq[(UnitOfMeasurement[Mass], UnitOfMeasurement[Mass], Bijection[N, N])] =
        List[(UnitOfMeasurement[Mass], UnitOfMeasurement[Mass], Bijection[N, N])](
          (tonne, megagram, BijectiveIdentity[N]),
          (milligram, gram, Scale10s(3)),
          (gram, kilogram, Scale10s(3)),
          (gram, megagram, Scale10s(6)),
          (tonne, kilotonne, Scale10s(3)),
          (tonne, megatonne, Scale10s(6)),
          (tonne, gigatonne, Scale10s(9)),
          (tonne, teratonne, Scale10s(12)),
          (tonne, petatonne, Scale10s(15)),
          (tonne, exatonne, Scale10s(18)),
          (tonne, zettatonne, Scale10s(21)),
          (tonne, yottatonne, Scale10s(24)))

    }
}