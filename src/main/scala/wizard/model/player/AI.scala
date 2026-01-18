package wizard.model.player

import wizard.model.cards.{Card, Color}

class AI private[player](name: String) extends Player(name) {
    
    override def bid(): Int =
        throw new NotImplementedError("AI bidding strategy not implemented yet")
    
    def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card =
        throw new NotImplementedError("AI playCard strategy not implemented yet")
    
    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int): Card =
        playCard(Option(leadColor), Option(trump), currentPlayerIndex)
}
