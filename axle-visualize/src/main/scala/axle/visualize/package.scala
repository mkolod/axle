
package axle

import java.awt.Component
import java.awt.image.BufferedImage
import java.io.File

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import axle.algebra.DirectedGraph
import axle.jung.JungDirectedGraph
import axle.jung.JungUndirectedGraph
import axle.pgm.BayesianNetwork
import axle.pgm.BayesianNetworkNode
import axle.quanta.Time
import axle.quanta.TimeConverter
import axle.quanta.UnittedQuantity
import axle.visualize.Draw
import axle.visualize.Fed
import axle.visualize.FrameRepaintingActor
import javax.imageio.ImageIO
import spire.algebra.Eq
import spire.algebra.Field

package object visualize {

  def draw[T: Draw](t: T): Unit = {
    val draw = implicitly[Draw[T]]
    val component = draw.component(t)
    val minSize = component.getMinimumSize
    val frame = AxleFrame(minSize.width, minSize.height)
    frame.initialize()
    val rc = frame.add(component)
    rc.setVisible(true)
    frame.setVisible(true)
  }

  def play[T: Draw, D](
    t: T,
    f: D => D,
    interval: UnittedQuantity[Time, Double])(
      implicit system: ActorSystem,
      tc: TimeConverter[Double]): ActorRef = {

    val draw = implicitly[Draw[T]]
    draw.component(t) match {
      case fed: Component with Fed[D] => {
        val minSize = fed.getMinimumSize
        val frame = AxleFrame(minSize.width, minSize.height)
        val feeder = fed.setFeeder(f, interval, system)
        system.actorOf(Props(classOf[FrameRepaintingActor], frame, fed.feeder.get))
        frame.initialize()
        val rc = frame.add(fed)
        rc.setVisible(true)
        frame.setVisible(true)
        feeder
      }
      case _ => null
    }
  }

  implicit def drawJungUndirectedGraph[VP: Show, EP: Show]: Draw[JungUndirectedGraph[VP, EP]] =
    new Draw[JungUndirectedGraph[VP, EP]] {
      def component(jug: JungUndirectedGraph[VP, EP]) =
        JungUndirectedGraphVisualization().component(jug)
    }

  implicit def drawJungDirectedGraph[VP: HtmlFrom, EP: Show]: Draw[JungDirectedGraph[VP, EP]] =
    new Draw[JungDirectedGraph[VP, EP]] {
      def component(jdg: JungDirectedGraph[VP, EP]) =
        JungDirectedGraphVisualization().component(jdg)
    }

  implicit def drawBayesianNetwork[T: Manifest: Eq, N: Field: Manifest: Eq, DG[_, _]: DirectedGraph](implicit drawDG: Draw[DG[BayesianNetworkNode[T, N], String]]): Draw[BayesianNetwork[T, N, DG]] = {
    new Draw[BayesianNetwork[T, N, DG]] {
      def component(bn: BayesianNetwork[T, N, DG]) =
        drawDG.component(bn.graph)
    }
  }

  /**
   * component2file
   *
   * encoding: PNG, JPEG, gif, BMP
   *
   * http://stackoverflow.com/questions/4028898/create-an-image-from-a-non-visible-awt-component
   */

  def draw2file[T: Draw](t: T, filename: String, encoding: String): Unit = {

    val component = implicitly[Draw[T]].component(t)

    val minSize = component.getMinimumSize
    val frame = AxleFrame(minSize.width, minSize.height)
    frame.setUndecorated(true)
    frame.initialize()
    val rc = frame.add(component)
    // rc.setVisible(true)
    frame.setVisible(true)

    val img = new BufferedImage(frame.getWidth, frame.getHeight, BufferedImage.TYPE_INT_RGB) // ARGB
    val g = img.createGraphics()
    frame.paintAll(g)

    ImageIO.write(img, encoding, new File(filename))

    g.dispose()
  }

  def png[T: Draw](t: T, filename: String): Unit = draw2file(t, filename, "PNG")

  def jpeg[T: Draw](t: T, filename: String): Unit = draw2file(t, filename, "JPEG")

  def gif[T: Draw](t: T, filename: String): Unit = draw2file(t, filename, "gif")

  def bmp[T: Draw](t: T, filename: String): Unit = draw2file(t, filename, "BMP")

}
