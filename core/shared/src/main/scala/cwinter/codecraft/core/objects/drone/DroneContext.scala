package cwinter.codecraft.core.objects.drone

import cwinter.codecraft.core.api.Player
import cwinter.codecraft.core.errors.Errors
import cwinter.codecraft.core.game.{CommandRecorder, DroneWorldSimulator, WorldConfig}
import cwinter.codecraft.core.objects.IDGenerator
import cwinter.codecraft.core.replay.{NullReplayRecorder, ReplayRecorder}
import cwinter.codecraft.graphics.engine.Debug
import cwinter.codecraft.util.maths.RNG


private[core] case class DroneContext(
  player: Player,
  worldConfig: WorldConfig,
  tickPeriod: Int,
  commandRecorder: Option[CommandRecorder],
  idGenerator: IDGenerator,
  rng: RNG,
  isLocallyComputed: Boolean,
  isMultiplayer: Boolean,
  simulator: DroneWorldSimulator,
  replayRecorder: ReplayRecorder = NullReplayRecorder,
  debug: Debug,
  errors: Errors
) {
  def settings = simulator.settings

  var missileHits = List.empty[MissileHit]
}

