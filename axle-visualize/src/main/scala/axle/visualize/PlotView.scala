package axle.visualize

import java.awt.Color.black
import java.awt.Font

import scala.Stream.continually

import axle.algebra.Plottable
import axle.quanta.Angle.{ ° => ° }
import axle.quanta.UnittedQuantity
import axle.visualize.element.DataLines
import axle.visualize.element.HorizontalLine
import axle.visualize.element.Key
import axle.visualize.element.VerticalLine
import axle.visualize.element.XTics
import axle.visualize.element.YTics
import spire.algebra.Eq
import spire.implicits.IntAlgebra
import spire.implicits.eqOps
import spire.math.Number.apply
import spire.implicits.DoubleAlgebra
import spire.implicits.moduleOps
import spire.compat.ordering

class PlotView[X: Plottable: Eq, Y: Plottable: Eq, D](plot: Plot[X, Y, D], data: Seq[(String, D)], normalFont: Font) {

  import plot._

  val colorStream = continually(colors.toStream).flatten

  val xPlottable = implicitly[Plottable[X]]
  implicit val xOrder = xPlottable.order
  val yPlottable = implicitly[Plottable[Y]]
  implicit val yOrder = yPlottable.order

  val keyOpt = if (drawKey) {
    Some(new Key(plot, normalFont, colorStream, keyWidth, keyTopPadding, data))
  } else {
    None
  }

  val minXCandidates = yAxis.toList ++ (data flatMap {
    case (label, d: D) => orderedXs(d).headOption
  })
  val minX = if (minXCandidates.size > 0) minXCandidates.min else xPlottable.zero

  val minYCandidates = xAxis.toList ++ (data flatMap {
    case (label, d: D) =>
      val xs = orderedXs(d)
      if (xs.size === 0)
        None
      else
        Some(xs map { x2y(d, _) } min)
  }) filter { yPlottable.isPlottable _ }

  val minY = if (minYCandidates.size > 0) minYCandidates.min else yPlottable.zero

  val maxXCandidates = yAxis.toList ++ (data flatMap {
    case (label, d: D) => orderedXs(d).lastOption
  })

  val maxX = if (minXCandidates.size > 0) maxXCandidates.max else xPlottable.zero

  val maxYCandidates = xAxis.toList ++ (data flatMap {
    case (label, d: D) => {
      val xs = orderedXs(d)
      if (xs.size === 0)
        None
      else
        Some(xs map { x2y(d, _) } max)
    }
  }) filter { yPlottable.isPlottable _ }

  val maxY = if (minYCandidates.size > 0) maxYCandidates.max else yPlottable.zero

  val minPoint = Point2D(minX, minY)
  val maxPoint = Point2D(maxX, maxY)

  val scaledArea = new ScaledArea2D(
    width = if (drawKey) width - (keyWidth + keyLeftPadding) else width,
    height, border,
    minPoint.x, maxPoint.x, minPoint.y, maxPoint.y)

  val vLine = new VerticalLine(scaledArea, yAxis.getOrElse(minX), black)
  val hLine = new HorizontalLine(scaledArea, xAxis.getOrElse(minY), black)
  val xTics = new XTics(scaledArea, xPlottable.tics(minX, maxX), normalFont, true, 0 *: °[Double], black)
  val yTics = new YTics(scaledArea, yPlottable.tics(minY, maxY), normalFont, black)

  val dataLines = new DataLines(scaledArea, data, orderedXs, x2y, colorStream, pointDiameter, connect)

}
