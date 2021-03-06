package cwinter.codecraft.graphics.engine

import cwinter.codecraft.graphics.model.{ClosedModel, Model}
import cwinter.codecraft.util.PrecomputedHashcode
import cwinter.codecraft.util.maths._
import cwinter.codecraft.util.maths.matrices.{Matrix4x4, RotationZTranslationXYMatrix4x4, RotationZTranslationXYTransposedMatrix4x4}


private[codecraft] trait WorldObjectDescriptor[T] extends PrecomputedHashcode {
  self: Product =>

  // making `ctx` private causes an issue with Scala.js
  // (it looks like `rs` gets inlined in subclasses, where `cxt` is not visible if private)
  protected var ctx: GraphicsContext = null
  implicit protected def rs: RenderStack = ctx.materials

  private var cachedModel = Option.empty[Model[T]]


  def intersects(xPos: Float, yPos: Float, rectangle: Rectangle): Boolean = true

  @inline
  final protected def intersects(x: Float, y: Float, width: Float, rectangle: Rectangle): Boolean = {
    x + width > rectangle.xMin &&
    x - width < rectangle.xMax &&
    y + width > rectangle.yMin &&
    y - width < rectangle.yMax
  }

  def model(timestep: Int, context: GraphicsContext): Model[T] = {
    cachedModel match {
      case Some(model) if ctx == context => model
      case _ =>
        ctx = context
        val model = getModel(context)
        if (allowCaching) cachedModel = Some(model)
        model
    }
  }

  protected def getModel(context: GraphicsContext): Model[T]
  protected def allowCaching: Boolean = true
}

