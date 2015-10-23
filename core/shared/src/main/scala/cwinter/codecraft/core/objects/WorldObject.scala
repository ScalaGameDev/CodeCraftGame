package cwinter.codecraft.core.objects

import cwinter.codecraft.collisions.Positionable
import cwinter.codecraft.core.SimulatorEvent
import cwinter.codecraft.graphics.worldstate.WorldObjectDescriptor
import cwinter.codecraft.util.maths.Vector2


private[core] trait WorldObject {
  def position: Vector2

  def update(): Seq[SimulatorEvent]
  private[core] def descriptor: Seq[WorldObjectDescriptor]
  private[core] val id = WorldObject.generateUID()
  private[core] def hasDied: Boolean
}


private[core] object WorldObject {
  private[this] var objectCount: Int = -1
  private def generateUID(): Int = {
    objectCount += 1
    objectCount
  }

  private[codecraft] def resetCount(): Unit = objectCount = -1

  implicit object WorldObjectIsPositionable extends Positionable[WorldObject] {
    override def position(t: WorldObject): Vector2 = t.position
  }
}
