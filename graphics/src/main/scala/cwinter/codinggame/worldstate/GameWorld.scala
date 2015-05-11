package cwinter.codinggame.worldstate

trait GameWorld {
  def worldState: Iterable[WorldObjectDescriptor]
  def update(): Unit
  def timestep: Int
}