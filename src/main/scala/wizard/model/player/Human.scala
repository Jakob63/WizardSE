package wizard.model.player

import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.model.player.Player

class Human private[player](name: String) extends Player(name) {

    override def bid(): Int = {
        val input = notifyObservers("bid einlesen").asInstanceOf[String]
        if (input == "" || input.trim.isEmpty || !input.forall(_.isDigit)) {
            notifyObservers("invalid input, bid again")
            return bid()
        }
        input.toInt
    }

    override def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int): Card = {
        val input = notifyObservers("card einlesen").asInstanceOf[String]
        val cardIndex = try {
            input.toInt
        } catch {
            case _: NumberFormatException => -1
        }
        if (cardIndex < 1 || cardIndex > hand.cards.length) {
            notifyObservers("invalid card")
            return playCard(leadColor, trump, currentPlayerIndex)
        }
        hand.cards(cardIndex - 1)
    }
}
