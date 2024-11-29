package wizard.model.player

import wizard.model.player.{AI, Human, Player}
import wizard.model.player.Director

enum PlayerType:
    case Human, AI
end PlayerType

object PlayerFactory {
    def createPlayer(name: String, playerType: PlayerType): Player = {
        val buildType: PlayerBuilder = playerType match {
            case PlayerType.Human => new BuildHuman()
            case PlayerType.AI => new BuildAI()
        }
        if (name == null) {
            Director.makeRandomNames(buildType)
        } else {
            Director.makeWithName(buildType, name)
        }
    }
}