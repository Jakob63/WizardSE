package wizard.Controller.control

import wizard.Model.player.Player
import wizard.Model.rounds.{Game, Round}

object GameLogic {
    
    def validGame(number: Int): Boolean = {
        number >= 3 && number <= 6
    }

    def playGame(game: Game, players: List[Player]): Unit = {
        for (i <- 1 to game.rounds) { // i = 1, 2, 3, ..., rounds
            game.currentround = i
            val round = new Round(players)
            RoundLogic.playRound(game.currentround, players: List[Player])
        }
    }

    // game is over if all rounds are played
    def isOver(game: Game): Boolean = {
        game.rounds == 0
    }
}
