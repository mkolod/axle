package axle.visualize

import javax.swing.JPanel
import java.awt.{ Color, Font, FontMetrics, Graphics, Graphics2D, Dimension }
import scala.concurrent.duration._
import axle.quanta._
import Angle._
import Color._
import collection._
import Stream.continually
import akka.pattern.ask
import akka.actor.{ Props, Actor, ActorRef, ActorSystem, ActorLogging }
import akka.util.Timeout
import collection.immutable.TreeMap
import scala.concurrent.duration._
import DataFeed._
import java.awt.Frame
import scala.concurrent.Await

class ReactivePlotView[X: Plottable, Y: Plottable](plot: ReactivePlot[X, Y], data: Seq[(String, SortedMap[X, Y])]) {

  import plot._

  val colorStream = continually(colors.toStream).flatten

  val xPlottable = implicitly[Plottable[X]]
  val yPlottable = implicitly[Plottable[Y]]

  val keyOpt = if (drawKey)
    Some(new Key(plot, colorStream, keyWidth, keyTopPadding, data))
  else
    None

  val minX = List(yAxis, data.map(_._2.firstKey).min(xPlottable)).min(xPlottable)
  val maxX = List(yAxis, data.map(_._2.lastKey).max(xPlottable)).max(xPlottable)
  val minY = List(xAxis, data.map(lf => (lf._2.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).min(yPlottable)).min(yPlottable)).min(yPlottable)
  val maxY = List(xAxis, data.map(lf => (lf._2.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).max(yPlottable)).max(yPlottable)).max(yPlottable)
  val minPoint = Point2D(minX, minY)
  val maxPoint = Point2D(maxX, maxY)

  val scaledArea = new ScaledArea2D(
    width = if (drawKey) width - (keyWidth + keyLeftPadding) else width,
    height, border,
    minPoint.x, maxPoint.x, minPoint.y, maxPoint.y
  )

  val vLine = new VerticalLine(scaledArea, yAxis, black)
  val hLine = new HorizontalLine(scaledArea, xAxis, black)
  val xTics = new XTics(scaledArea, xPlottable.tics(minX, maxX), black)
  val yTics = new YTics(scaledArea, yPlottable.tics(minY, maxY), black)

  val dataLines = new DataLines(scaledArea, data, colorStream, pointDiameter, connect)

}

class ReactivePlotComponent[X: Plottable, Y: Plottable](plot: ReactivePlot[X, Y]) extends JPanel {

  import plot._

  setMinimumSize(new Dimension(width, height))

  val normalFont = new Font(fontName, Font.BOLD, fontSize)
  val xAxisLabelText = xAxisLabel.map(new Text(_, normalFont, width / 2, height - border / 2))
  val yAxisLabelText = yAxisLabel.map(new Text(_, normalFont, 20, height / 2, angle = Some(90 *: °)))
  val titleFont = new Font(titleFontName, Font.BOLD, titleFontSize)
  val titleText = title.map(new Text(_, titleFont, width / 2, 20))

  implicit val askTimeout = Timeout(1.second)
  var timestamp = 0L

  override def paintComponent(g: Graphics): Unit = {

    val g2d = g.asInstanceOf[Graphics2D]

    val update = dataFeedActor ? Fetch(timestamp)

    val dataOpt = Await.result(update, 1.seconds).asInstanceOf[Option[List[(String, TreeMap[X, Y])]]]
    
    dataOpt.map(data => {

      timestamp = System.currentTimeMillis

      val view = new ReactivePlotView(plot, data)

      import view._

      val paintables =
        Vector(vLine, hLine, xTics, yTics, dataLines) ++
          Vector(titleText, xAxisLabelText, yAxisLabelText, view.keyOpt).flatMap(i => i)

      for (paintable <- paintables) {
        paintable.paint(g2d)
      }
    })

  }

}
