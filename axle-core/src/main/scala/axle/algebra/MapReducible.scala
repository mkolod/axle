package axle.algebra

import scala.reflect.ClassTag

trait MapReducible[M[_]] {

  def mapReduce[A: ClassTag, B: ClassTag, K: ClassTag](
    input: M[A],
    mapper: A => (K, B),
    zero: B,
    op: (B, B) => B): M[(K, B)]
}

object MapReducible {

  implicit def mapReduceSeq: MapReducible[Seq] = new MapReducible[Seq] {

    def mapReduce[A: ClassTag, B: ClassTag, K: ClassTag](
      input: Seq[A],
      mapper: A => (K, B),
      zero: B,
      reduce: (B, B) => B): Seq[(K, B)] = ???
  }
}
