package axle.algebra

import scala.reflect.ClassTag
import scala.collection.parallel.immutable.ParSeq

import spire.algebra.Eq
import spire.algebra.Ring
import spire.implicits.MapRng
import spire.implicits.additiveSemigroupOps

trait Aggregatable[F[_]] {

  def aggregate[A, B: ClassTag](xs: F[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B

  def tally[A: Eq, N: Ring](xs: F[A]): Map[A, N] = {
    val ring = implicitly[Ring[N]]
    aggregate(xs)(Map.empty[A, N].withDefaultValue(ring.zero))(
      (m, x) => m + (x -> ring.plus(m(x), ring.one)),
      _ + _)
  }

}

object Aggregatable {

  implicit def aggregatableSeq = new Aggregatable[Seq] {
    def aggregate[A, B: ClassTag](as: Seq[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
      as.aggregate(zeroValue)(seqOp, combOp)
  }

  implicit def aggregatableList = new Aggregatable[List] {
    def aggregate[A, B: ClassTag](as: List[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
      as.aggregate(zeroValue)(seqOp, combOp)
  }

  implicit def aggregatableParSeq = new Aggregatable[ParSeq] {
    def aggregate[A, B: ClassTag](ps: ParSeq[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
      ps.aggregate(zeroValue)(seqOp, combOp)
  }

  implicit def aggregatableIndexedSeq = new Aggregatable[scala.collection.immutable.IndexedSeq] {
    def aggregate[A, B: ClassTag](is: scala.collection.immutable.IndexedSeq[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
      is.aggregate(zeroValue)(seqOp, combOp)
  }
  
}
