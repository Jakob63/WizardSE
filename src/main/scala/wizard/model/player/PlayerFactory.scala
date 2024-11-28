package wizard.model.player

import wizard.model.player.{AI, Human, Player}

enum PlayerType:
    case Human, AI
end PlayerType

object PlayerFactory {
    def createPlayer(name: String, playerType: PlayerType): Player = {
        playerType match {
            case PlayerType.Human =>  Human(name)
            case PlayerType.AI =>  AI(name)
        }
    }
}


