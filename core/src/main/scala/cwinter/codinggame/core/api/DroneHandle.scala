package cwinter.codinggame.core.api

import cwinter.codinggame.core.objects.drone.Drone
import cwinter.codinggame.util.maths.Vector2
import cwinter.codinggame.worldstate.Player


trait DroneHandle {
  def position: Vector2
  def spec: DroneSpec
  def weaponsCooldown: Int
  def isVisible: Boolean
  def player: Player
  def hitpoints: Int
  def isEnemy: Boolean

  private[core] def drone: Drone
}