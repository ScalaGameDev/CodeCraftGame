package cwinter.codecraft.core.multiplayer

import akka.actor._
import akka.io.IO
import cwinter.codecraft.core.api.{BluePlayer, OrangePlayer, Player, TheGameMaster}
import cwinter.codecraft.core.replay.DummyDroneController
import cwinter.codecraft.core.{AuthoritativeServerConfig, DroneWorldSimulator}
import spray.can.Http
import spray.can.server.UHttp


object Server {
  def spawnServerInstance(displayGame: Boolean = false): Unit = {
    implicit val system = ActorSystem()

    val server = system.actorOf(MultiplayerServer.props(displayGame), "websocket")

    IO(UHttp) ! Http.Bind(server, "0.0.0.0", 8080)

    system.awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    spawnServerInstance(false)
  }
}


class MultiplayerServer(displayGame: Boolean = false) extends Actor with ActorLogging {
  val map = TheGameMaster.defaultMap()
  val clientPlayers = Set[Player](BluePlayer)
  val serverPlayers = Set[Player](OrangePlayer)


  def receive = {
    // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val worker = new RemoteWebsocketClient(clientPlayers, map)
      val conn = context.actorOf(WebsocketActor.props(serverConnection, worker))

      val server = new DroneWorldSimulator(
        map,
        Seq(new DummyDroneController, TheGameMaster.level2AI()),
        t => Seq.empty,
        None,
        AuthoritativeServerConfig(serverPlayers, clientPlayers, Set(worker))
      )

      serverConnection ! Http.Register(conn)

      if (displayGame) {
        TheGameMaster.run(server)
      } else {
        server.run()
      }
  }
}


object MultiplayerServer {
  def props(displayGame: Boolean = false) = Props(classOf[MultiplayerServer], displayGame)
}

