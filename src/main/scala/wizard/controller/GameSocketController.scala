package wizard.controller

import javax.inject.Inject
import org.apache.pekko.stream.Materializer
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import wizard.actors.GameSocketActor
import wizard.controller.controllerBaseImpl.{BaseGameLogic, BasePlayerLogic, BaseRoundLogic}
import util.QueueInput

class GameSocketController @Inject() (
  cc: ControllerComponents
)(using
  actorSystem: ActorSystem,
  mat: Materializer
) extends AbstractController(cc) {

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef { out =>
      val gameLogic   = new BaseGameLogic()
      val playerLogic = new BasePlayerLogic()
      val roundLogic  = new BaseRoundLogic()
      val input       = new QueueInput()

      gameLogic.roundLogic = roundLogic
      roundLogic.playerLogic = playerLogic
      roundLogic.gameLogic = gameLogic
      playerLogic.gameLogic = gameLogic

      GameSocketActor.props(gameLogic, out, playerLogic, roundLogic, input)
    }
  }
}
