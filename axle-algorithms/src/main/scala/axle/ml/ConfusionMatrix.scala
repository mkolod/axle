package axle.ml

import axle.Show
import axle.algebra.Matrix
import axle.algebra.Functor
import axle.algebra.Finite
import axle.algebra.SetFrom
import axle.algebra.MapReducible
import axle.algebra.MapFrom
import axle.algebra.FunctionPair
import axle.syntax.matrix._
import spire.algebra._
import scala.reflect.ClassTag
import math.{ ceil, log10 }

case class ConfusionMatrix[T: ClassTag, CLASS: Order, L: Order: ClassTag, F[_]: Functor: Finite: SetFrom: MapReducible: MapFrom, M[_]](
  classifier: Classifier[T, CLASS],
  data: F[T],
  labelExtractor: T => L)(implicit val evMatrix: Matrix[M]) {

  val func = implicitly[Functor[F]]
  val sz = implicitly[Finite[F]]
  val settable = implicitly[SetFrom[F]]
  val mr = implicitly[MapReducible[F]]
  val mf = implicitly[MapFrom[F]]

  val label2clusterId = func.map(data)(datum => (labelExtractor(datum), classifier(datum)))

  val labelList: List[L] = settable.toSet(func.map(label2clusterId)(_._1)).toList
  val labelIndices: Map[L, Int] = labelList.zipWithIndex.toMap

  val labelIdClusterId2count =
    mf.toMap(
      mr.mapReduce[(L, CLASS), Int, (Int, CLASS)](
        label2clusterId,
        (lc: (L, CLASS)) => ((labelIndices(lc._1), lc._2), 1),
        0,
        _ + _)).withDefaultValue(0)

  val classes = classifier.classes

  val counts = evMatrix.matrix[Int](
    labelList.length,
    classes.size,
    (r: Int, c: Int) => labelIdClusterId2count((r, classes(c))))

  val width = ceil(log10(sz.size(data))).toInt

  val formatNumber = (i: Int) => ("%" + width + "d").format(i)

  lazy val rowSums = counts.rowSums
  lazy val columnSums = counts.columnSums

}

object ConfusionMatrix {

  implicit def showCM[T, CLASS, L, F[_], M[_]]: Show[ConfusionMatrix[T, CLASS, L, F, M]] =
    new Show[ConfusionMatrix[T, CLASS, L, F, M]] {

      def text(cm: ConfusionMatrix[T, CLASS, L, F, M]): String = {
        val matrix = cm.evMatrix
        (cm.labelList.zipWithIndex.map({
          case (label, r) => ((0 until matrix.columns(cm.counts)).map(c => cm.formatNumber(matrix.get(cm.counts)(r, c))).mkString(" ") + " : " + cm.formatNumber(matrix.get(cm.rowSums)(r, 0)) + " " + label + "\n")
        }).mkString("")) + "\n" +
          (0 until matrix.columns(cm.counts)).map(c => cm.formatNumber(matrix.get(cm.columnSums)(0, c))).mkString(" ") + "\n"
      }

    }

}
