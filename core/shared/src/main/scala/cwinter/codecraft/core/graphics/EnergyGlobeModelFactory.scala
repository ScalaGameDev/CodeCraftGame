package cwinter.codecraft.core.graphics

import cwinter.codecraft.graphics.engine.RenderStack
import cwinter.codecraft.graphics.primitives.Polygon
import cwinter.codecraft.util.maths.{ColorRGB, ColorRGBA, VertexXY}


private[graphics] object EnergyGlobeModelFactory {
  def build(position: VertexXY, fade: Float = 1)(implicit rs: RenderStack) = {
    if (fade == 1) {
      Polygon(
        material = rs.BloomShader,
        n = 7,
        colorMidpoint = ColorRGB(1, 1, 1),
        colorOutside = ColorRGB(0, 1, 0),
        radius = 2,
        position = position,
        zPos = 3
      )
    } else {
      Polygon(
        material = rs.TranslucentProportional,
        n = 7,
        colorMidpoint = ColorRGBA(1, 1, 1, fade),
        colorOutside = ColorRGBA(0, 1, 0, fade),
        radius = 2,
        position = position,
        zPos = 3
      )
    }
  }
}
