package axle

import scala.Vector
import scala.collection.GenTraversable

import axle.algebra.DirectedGraph
import axle.stats.Case
import axle.stats.ConditionalProbabilityTable0
import axle.stats.Distribution
import axle.stats.EnrichedCaseGenTraversable
import axle.stats.P
import axle.quanta.UnittedQuantity
import axle.quanta.Information
import spire.algebra.AdditiveMonoid
import spire.algebra.Eq
import spire.algebra.Field
import spire.algebra.NRoot
import spire.algebra.Order
import spire.algebra.Ring
import spire.implicits.additiveGroupOps
import spire.implicits.convertableOps
import spire.implicits.literalIntAdditiveGroupOps
import spire.implicits.moduleOps
import spire.implicits.multiplicativeGroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.implicits.nrootOps
import spire.implicits.orderOps
import spire.implicits.semiringOps
import spire.implicits._
import spire.math.ConvertableFrom
import spire.math.ConvertableTo
import spire.math.Number
import spire.math.Rational
import spire.math.Real
import spire.random.Dist
import spire.optional.unicode.Σ
import axle.quanta.UnitConverter
import axle.quanta.InformationConverter

package object stats {

  implicit val rationalProbabilityDist: Dist[Rational] = {
    val biggishInt = 1000000
    Dist(Rational(_: Int, biggishInt))(Dist.intrange(0, biggishInt))
  }

  //implicit def evalProbability[N]: Probability[N] => N = _()

  implicit def enrichCaseGenTraversable[A: Manifest, N: Field](cgt: Iterable[Case[A, N]]): EnrichedCaseGenTraversable[A, N] = EnrichedCaseGenTraversable(cgt)

  val sides = Vector('HEAD, 'TAIL)

  def coin(pHead: Rational = Rational(1, 2)): Distribution[Symbol, Rational] =
    ConditionalProbabilityTable0[Symbol, Rational](
      Map('HEAD -> pHead, 'TAIL -> (1 - pHead)), "coin")

  def binaryDecision(yes: Rational): Distribution0[Boolean, Rational] =
    ConditionalProbabilityTable0(Map(true -> yes, false -> (1 - yes)), s"binaryDecision $yes")

  def uniformDistribution[T](values: Seq[T], name: String): Distribution0[T, Rational] = {

    val dist = values.groupBy(identity).mapValues({ ks => Rational(ks.size, values.size) }).toMap

    ConditionalProbabilityTable0(dist, name)
  }

  def iffy[C: Eq, N: Field: Order: Dist](
    decision: Distribution0[Boolean, N],
    trueBranch: Distribution[C, N],
    falseBranch: Distribution[C, N]): Distribution0[C, N] = {

    val addN = implicitly[AdditiveMonoid[N]]
    val pTrue = decision.probabilityOf(true)
    val pFalse = decision.probabilityOf(false)

    val parts = (trueBranch.values.map(v => (v, trueBranch.probabilityOf(v) * pTrue)) ++
      falseBranch.values.map(v => (v, falseBranch.probabilityOf(v) * pFalse)))

    val newDist = parts.groupBy(_._1).mapValues(xs => xs.map(_._2).reduce(addN.plus)).toMap

    ConditionalProbabilityTable0(newDist, "todo")
  }

  def log2[N: Field: ConvertableFrom](x: N) = math.log(x.toDouble) / math.log(2)

  def mean[N: Field: Manifest](xs: Iterable[N]): N = Σ(xs) / xs.size

  def square[N: Ring](x: N): N = x ** 2

  /**
   * http://en.wikipedia.org/wiki/Standard_deviation
   */

  def standardDeviation[A: NRoot: Field: Manifest: ConvertableTo, N: Field: Manifest: ConvertableFrom](distribution: Distribution[A, N]): A = {

    def n2a(n: N): A = implicitly[ConvertableFrom[N]].toType[A](n)(implicitly[ConvertableTo[A]])

    val μ: A = Σ(distribution.values map { x => n2a(distribution.probabilityOf(x)) * x })

    Σ(distribution.values map { x => n2a(distribution.probabilityOf(x)) * square(x - μ) }).sqrt
  }

  def σ[A: NRoot: Field: Manifest: ConvertableTo, N: Field: Manifest: ConvertableFrom](distribution: Distribution[A, N]): A =
    standardDeviation(distribution)

  def stddev[A: NRoot: Field: Manifest: ConvertableTo, N: Field: Manifest: ConvertableFrom](distribution: Distribution[A, N]): A =
    standardDeviation(distribution)

  def entropy[A: Manifest, N: Field: Eq: ConvertableFrom](
    X: Distribution[A, N])(
      implicit convert: InformationConverter[Double]): UnittedQuantity[Information, Double] = {

    val convertN = implicitly[ConvertableFrom[N]]
    val H = Σ(X.values map { x =>
      val px: N = axle.stats.P(X is x).apply()
      if (px === implicitly[Field[N]].zero) {
        0d
      } else {
        convertN.toDouble(-px) * log2(px)
      }
    })
    UnittedQuantity(H, convert.bit)
  }

  def H[A: Manifest, N: Field: Eq: ConvertableFrom](
    X: Distribution[A, N])(implicit convert: InformationConverter[Double]): UnittedQuantity[Information, Double] =
    entropy(X)

}
