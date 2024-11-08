package wizard.rounds

import wizard.player.Player

case class Game(players : List[Player], rounds : Int) {
    players.foreach(player => player.hand = List())
    
}
