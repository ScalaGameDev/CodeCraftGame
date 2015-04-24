package cwinter.graphics.models

import cwinter.codinggame.util.maths.{ColorRGBA, VertexXY}
import cwinter.graphics.engine.RenderStack
import cwinter.graphics.model._


object LaserMissileModelFactory {
  def build(positions: Seq[(Float, Float)])(implicit rs: RenderStack) = {
    if (positions.length < 2) EmptyModel
    else {
      val midpoints = positions.map { case (x, y) => VertexXY(x, y)}
      val n = positions.length
      val colors = positions.zipWithIndex.map {
        case (_, index) => ColorRGBA(1, 1, 1, index / n.toFloat)
      }

      QuadStrip(
        rs.TranslucentAdditive,
        midpoints,
        colors,
        2
      ).noCaching.getModel
    }
  }
}