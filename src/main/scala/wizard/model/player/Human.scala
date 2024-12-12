package wizard.model.player

import wizard.model.cards.{Card, Color}
import scala.util.{Failure, Success, Try}

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
        val cardIndex = Try(input.toInt) match {
            case Success(index) => index
            case Failure(_) => -1
        }
        if (cardIndex < 1 || cardIndex > hand.cards.length) {
            notifyObservers("invalid card")
            return playCard(leadColor, trump, currentPlayerIndex)
        }
        hand.cards(cardIndex - 1)
    }
}
