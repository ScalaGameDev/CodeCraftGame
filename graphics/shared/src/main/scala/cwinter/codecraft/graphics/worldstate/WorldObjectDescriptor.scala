package cwinter.codecraft.graphics.worldstate

import cwinter.codecraft.graphics.engine.RenderStack
import cwinter.codecraft.graphics.model.{ClosedModel, Model}
import cwinter.codecraft.graphics.models._
import cwinter.codecraft.graphics.primitives.PolygonRing
import cwinter.codecraft.util.maths.matrices.{RotationZTranslationXYMatrix4x4, RotationZTranslationXYTransposedMatrix4x4, Matrix4x4}
import cwinter.codecraft.util.maths._
import cwinter.codecraft.util.{PrecomputedHashcode, maths}


private[codecraft] case class ModelDescriptor[T](
  position: PositionDescriptor,
  objectDescriptor: WorldObjectDescriptor[T],
  objectParameters: T
) {
  @inline
  final def intersects(rectangle: Rectangle): Boolean =
    objectDescriptor.intersects(position.x, position.y, rectangle)


  def closedModel(timestep: Int)(implicit rs: RenderStack): ClosedModel[T] =
    new ClosedModel[T](objectParameters, objectDescriptor.model(timestep), modelview)

  private def modelview(implicit renderStack: RenderStack): Matrix4x4 = {
    if (position.cachedModelviewMatrix.isEmpty) {
      val xPos = position.x
      val yPos = position.y
      val orientation = position.orientation
      val modelviewMatrix =
        if (renderStack.modelviewTranspose) new RotationZTranslationXYTransposedMatrix4x4(orientation, xPos, yPos)
        else new RotationZTranslationXYMatrix4x4(orientation, xPos, yPos)
      position.cachedModelviewMatrix = modelviewMatrix
    }
    position.cachedModelviewMatrix.get
  }
}

private[codecraft] object ModelDescriptor {
  def apply(position: PositionDescriptor, objectDescriptor: WorldObjectDescriptor[Unit]): ModelDescriptor[Unit] =
    ModelDescriptor(position, objectDescriptor, Unit)
}

private[codecraft] case class PositionDescriptor(
  x: Float,
  y: Float,
  orientation: Float = 0
) {
  assert(!x.toDouble.isNaN)
  assert(!y.toDouble.isNaN)
  assert(!orientation.toDouble.isNaN)

  private[this] var _cachedModelviewMatrix: Option[Matrix4x4] = None
  private[graphics] def cachedModelviewMatrix_=(value: Matrix4x4): Unit =
    _cachedModelviewMatrix = Some(value)
  private[graphics] def cachedModelviewMatrix = _cachedModelviewMatrix
}

private[codecraft] object NullPositionDescriptor extends PositionDescriptor(0, 0, 0)

private[codecraft] trait WorldObjectDescriptor[T] extends PrecomputedHashcode {
  self: Product =>

  def intersects(xPos: Float, yPos: Float, rectangle: Rectangle): Boolean = true

  @inline
  final protected def intersects(x: Float, y: Float, width: Float, rectangle: Rectangle): Boolean = {
    x + width > rectangle.xMin &&
    x - width < rectangle.xMax &&
    y + width > rectangle.yMin &&
    y - width < rectangle.yMax
  }

  def model(timestep: Int)(implicit rs: RenderStack): Model[T] = {
    cachedModel match {
      case Some(model) => model
      case None =>
        val model = createModel(timestep)
        // FIXME: special case required for models that are not cached. need to rework caching to fix this properly.
        if (!this.isInstanceOf[HomingMissileDescriptor] && !this.isInstanceOf[DrawCircleOutline] &&
          !this.isInstanceOf[BasicHomingMissileDescriptor])
          this.cachedModel = model
        model
    }
  }


  private[this] var _cachedModel: Option[Model[T]] = None
  private[this] def cachedModel_=(value: Model[T]): Unit = _cachedModel = Some(value)
  private[this] def cachedModel: Option[Model[T]] = _cachedModel


  protected def createModel(timestep: Int)(implicit rs: RenderStack): Model[T]
}


private[codecraft] case class DroneDescriptor(
  sides: Int,
  modules: Seq[DroneModuleDescriptor],
  hasShields: Boolean,
  hullState: Seq[Byte],
  isBuilding: Boolean,
  animationTime: Int,
  playerColor: ColorRGB
) extends WorldObjectDescriptor[DroneModelParameters] {
  assert(hullState.size == sides - 1)

  override def intersects(xPos: Float, yPos: Float, rectangle: Rectangle) =
    intersects(xPos, yPos, 100, rectangle) // FIXME

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new DroneModelBuilder(this, timestep).getModel
}

private[codecraft] case class DroneModelParameters(
  shieldState: Option[Float],
  constructionState: Option[Float0To1] = None
)


private[codecraft] sealed trait DroneModuleDescriptor

private[codecraft] case class StorageModuleDescriptor(
  position: Int,
  contents: StorageModuleContents
) extends DroneModuleDescriptor


private[codecraft] sealed trait StorageModuleContents
private[codecraft] case object EmptyStorage extends StorageModuleContents
private[codecraft] case object MineralStorage extends StorageModuleContents
private[codecraft] case class EnergyStorage(filledPositions: Set[Int] = Set(0, 1, 2, 3, 4, 5, 6)) extends StorageModuleContents

private[codecraft] case class EnginesDescriptor(position: Int) extends DroneModuleDescriptor
private[codecraft] case class ShieldGeneratorDescriptor(position: Int) extends DroneModuleDescriptor
private[codecraft] case class MissileBatteryDescriptor(position: Int, n: Int = 3) extends DroneModuleDescriptor
private[codecraft] case class ManipulatorDescriptor(position: Int) extends DroneModuleDescriptor

private[codecraft] case class HarvestingBeamsDescriptor(
  droneSize: Int,
  moduleIndices: Seq[Int],
  mineralDisplacement: Vector2
) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    HarvestingBeamModelBuilder(this).getModel
}

private[codecraft] case class ConstructionBeamDescriptor(
  droneSize: Int,
  modules: Seq[(Int, Boolean)],
  constructionDisplacement: Vector2,
  playerColor: ColorRGB
) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    ConstructionBeamsModelBuilder(this).getModel
}

private[codecraft] case class EnergyGlobeDescriptor(
  fade: Float
) extends WorldObjectDescriptor[Unit] {
  assert(fade >= 0)
  assert(fade <= 1)

  override def intersects(xPos: Float, yPos: Float, rectangle: Rectangle): Boolean =
    intersects(xPos, yPos, 20, rectangle) // FIXME

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new EnergyGlobeModelBuilder(this).getModel
}

private[codecraft] case class CollisionMarker(
  radius: Float,
  orientation: Float
) extends WorldObjectDescriptor[Float] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    CollisionMarkerModelBuilder(this).getModel
}

private[codecraft] object PlainEnergyGlobeDescriptor extends EnergyGlobeDescriptor(1)

private[codecraft] case class MineralDescriptor(size: Int, xPos: Float, yPos: Float, orientation: Float)
  extends WorldObjectDescriptor[Unit] {

  override def intersects(xPos: Float, yPos: Float, rectangle: Rectangle): Boolean =
    intersects(this.xPos, this.yPos, 50, rectangle)

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new MineralModelBuilder(this).getModel
}


private[codecraft] case class LightFlashDescriptor(stage: Float)
  extends WorldObjectDescriptor[LightFlashDescriptor] {

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new LightFlashModelBuilder().getModel
}


private[codecraft] case class HomingMissileDescriptor(
  positions: Seq[(Float, Float)],
  maxPos: Int,
  playerColor: ColorRGB
) extends WorldObjectDescriptor[Unit] {

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    HomingMissileModelFactory.build(positions, maxPos, playerColor)
}


private[codecraft] case class BasicHomingMissileDescriptor(
  x: Float,
  y: Float,
  playerColor: ColorRGB
) extends WorldObjectDescriptor[Unit] {

  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    BasicHomingMissileModelFactory.build(x, y, playerColor)
}

private[codecraft] case class TestingObject(time: Int) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new TestModelBuilder(time).getModel
}

private[codecraft] case class DrawCircle(
  radius: Float,
  identifier: Int
) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    CircleModelBuilder(radius, identifier).getModel
}


private[codecraft] case class DrawCircleOutline(
  radius: Float,
  color: ColorRGB = ColorRGB(1, 1, 1)
) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    new PolygonRing(
      rs.MaterialXYZRGB, 40, Seq.fill(40)(color), Seq.fill(40)(color),
      radius - 2, radius, VertexXY(0, 0), 0, 0
    ).noCaching.getModel
}


private[codecraft] case class DrawRectangle(
  bounds: maths.Rectangle
) extends WorldObjectDescriptor[Unit] {
  override protected def createModel(timestep: Int)(implicit rs: RenderStack) =
    RectangleModelBuilder(bounds).getModel
}

