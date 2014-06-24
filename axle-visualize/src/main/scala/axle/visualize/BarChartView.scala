package axle.visualize

import java.awt.Color
import java.awt.Color.black
import java.awt.Font

import axle.algebra.Plottable
import axle.algebra.Plottable.DoublePlottable
import axle.quanta.Angle._
import axle.visualize.element.HorizontalLine
import axle.visualize.element.Rectangle
import axle.visualize.element.VerticalLine
import axle.visualize.element.XTics
import axle.visualize.element.YTics
import spire.algebra.Eq
import spire.implicits.DoubleAlgebra
import spire.math.Number.apply

class BarChartView[S, Y: Plottable: Eq](chart: BarChart[S, Y], data: Map[S, Y], colorStream: Stream[Color], normalFont: Font) {

  import chart._

  val minX = 0d
  val maxX = 1d
  val yAxis = minX

  val padding = 0.05 // on each side
  val widthPerSlice = (1d - (2 * padding)) / slices.size
  val whiteSpace = widthPerSlice * (1d - barWidthPercent)

  val yPlottable = implicitly[Plottable[Y]]

  val minY = List(xAxis, slices.map(s => (List(data(s)) ++ List(yPlottable.zero)).filter(yPlottable.isPlottable).min).min).min
  val maxY = List(xAxis, slices.map(s => (List(data(s)) ++ List(yPlottable.zero)).filter(yPlottable.isPlottable).max).max).max

  val scaledArea = new ScaledArea2D(
    width = if (drawKey) width - (keyWidth + keyLeftPadding) else width,
    height,
    border,
    minX, maxX, minY, maxY)

  val vLine = new VerticalLine(scaledArea, yAxis, black)
  val hLine = new HorizontalLine(scaledArea, xAxis, black)

  val gTics = new XTics(
    scaledArea,
    slices.zipWithIndex.map({ case (s, i) => (padding + (i + 0.5) * widthPerSlice, sLabeller(s)) }).toList,
    normalFont,
    false,
    36 *: °,
    black)

  val yTics = new YTics(scaledArea, yPlottable.tics(minY, maxY), normalFont, black)

  val bars = slices.zipWithIndex.zip(colorStream).map({
    case ((s, i), color) => {
      val leftX = padding + (whiteSpace / 2d) + i * widthPerSlice
      val rightX = leftX + widthPerSlice
      Rectangle(scaledArea, Point2D(leftX, minY), Point2D(rightX, data(s)), fillColor = Some(color))
    }
  })

}