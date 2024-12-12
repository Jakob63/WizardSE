package wizard.model.player

import wizard.model.player.Player
import wizard.model.cards.{Card, Color}

class AI private[player](name: String) extends Player(name) {
    
    override def bid(): Int = ???
    override def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int): Card = ???
}
