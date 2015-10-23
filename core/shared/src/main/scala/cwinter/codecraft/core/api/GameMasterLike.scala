package cwinter.codecraft.core.api

import cwinter.codecraft.core._
import cwinter.codecraft.core.ai.basicplus
import cwinter.codecraft.core.ai.cheese.Mothership
import cwinter.codecraft.util.maths.{Rectangle, Vector2}


private[codecraft] trait GameMasterLike {
  /**
   * Default dimensions for the size of the game world.
   */
  final val DefaultWorldSize = Rectangle(-4000, 4000, -2500, 2500)

  /**
   * Default resource distribution.
   */
  final val DefaultResourceDistribution = Seq(
      (20, 1), (20, 1), (20, 1), (20, 1),
      (20, 2), (20, 2),
      (15, 3), (15, 3),
      (15, 4), (15, 4)
    )

  /**
   * Default number of modules for the initial mothership.
   */
  final val DefaultMothership = new DroneSpec(
    missileBatteries = 2,
    constructors = 2,
    refineries = 3,
    storageModules = 3
  )


  private def constructSpawns(
    mothership1: DroneControllerBase,
    pos1: Vector2,
    mothership2: DroneControllerBase,
    pos2: Vector2
  ): Seq[Spawn] = {
    val spawn1 = Spawn(DefaultMothership, mothership1, pos1, BluePlayer, 21)
    val spawn2 = Spawn(DefaultMothership, mothership2, pos2, OrangePlayer, 21)
    Seq(spawn1, spawn2)
  }


  def run(simulator: DroneWorldSimulator): DroneWorldSimulator

  def createSimulator(
    mothership1: DroneControllerBase,
    mothership2: DroneControllerBase,
    worldSize: Rectangle,
    resourceClusters: Seq[(Int, Int)],
    spawn1: Vector2,
    spawn2: Vector2
  ): DroneWorldSimulator = {
    val spawns = constructSpawns(mothership1, spawn1, mothership2, spawn2)
    val map = WorldMap(worldSize, resourceClusters, spawns).withDefaultWinConditions
    new DroneWorldSimulator(map, devEvents)
  }

  /**
   * Starts a new game with two players.
   *
   * @param mothership1 The controller for the initial mothership of player 1.
   * @param mothership2 The controller for the initial mothership of player 2.
   */
  def startGame(mothership1: DroneControllerBase, mothership2: DroneControllerBase): DroneWorldSimulator = {
    val map = defaultMap(mothership1, mothership2)
    val simulator = new DroneWorldSimulator(map, devEvents)
    run(simulator)
    simulator
  }

  /**
   * Returns a [[WorldMap]] for the first level.
   *
   * @param mothership1 The controller for the initial mothership of player 1.
   */
  def level1Map(mothership1: DroneControllerBase): WorldMap = {
    val worldSize = Rectangle(-2000, 2000, -1000, 1000)
    val spawns = constructSpawns(mothership1, Vector2(1000, 200), new ai.basic.Mothership, Vector2(-1000, -200))
    WorldMap(worldSize, 100, spawns).withDefaultWinConditions
  }

  /**
   * Returns a [[WorldMap]] for the second level.
   *
   * @param mothership1 The controller for the initial mothership of player 1.
   * @return
   */
  def level2Map(mothership1: DroneControllerBase): WorldMap =
    defaultMap(mothership1, new basicplus.Mothership)

  /**
   * Returns a [[WorldMap]] for the bonus level.
   *
   * @param mothership1 The controller for the initial mothership for player 1.
   */
  def bonusLevelMap(mothership1: DroneControllerBase): WorldMap =
    defaultMap(mothership1, new ai.cheese.Mothership)

  /**
   * Returns the default [[WorldMap]].
   *
   * @param mothership1 The controller for the initial mothership of player 1.
   * @param mothership2 The controller for the initial mothership of player 2.
   */
  def defaultMap(mothership1: DroneControllerBase, mothership2: DroneControllerBase): WorldMap = {
    val worldSize = DefaultWorldSize
    val resourceClusters = DefaultResourceDistribution
    val spawns = constructSpawns(mothership1, Vector2(2500, 500), mothership2, Vector2(-2500, -500))
    WorldMap(worldSize, resourceClusters, spawns).withDefaultWinConditions
  }

  /**
   * Runs the first level.
   *
   * @param mothership1 The controller for your mothership.
   */
  def runLevel1(mothership1: DroneControllerBase): DroneWorldSimulator = {
    val map = level1Map(mothership1)
    val simulator = new DroneWorldSimulator(map, devEvents)
    run(simulator)
    simulator
  }

  /**
   * Runs the second level.
   *
   * @param mothership The controller for your mothership.
   */
  def runLevel2(mothership: DroneControllerBase): DroneWorldSimulator = {
    startGame(mothership, new Mothership)
  }

  /**
   * Runs the third level.
   *
   * @param mothership The controller for your mothership.
   */
  def runLevel3(mothership: DroneControllerBase): DroneWorldSimulator = {
    startGame(mothership, new ai.basicplus.Mothership)
  }

  /**
   * Runs a game with the level 1 AI versus the level 2 AI.
   */
  def runL1vL2(): DroneWorldSimulator = {
    startGame(new ai.basic.Mothership, new Mothership)
  }

  /**
   * Runs a game with the level 3 AI versus the level 3 AI.
   */
  def runL3vL3(): DroneWorldSimulator = {
    startGame(new ai.basicplus.Mothership, new ai.basicplus.Mothership)
  }

  protected var devEvents: Int => Seq[SimulatorEvent] = t => Seq()
  protected[codecraft] def setDevEvents(generator: Int => Seq[SimulatorEvent]): Unit = {
    devEvents = generator
  }
}

