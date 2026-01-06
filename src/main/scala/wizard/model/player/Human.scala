package wizard.model.player

import wizard.model.cards.{Card, Color}
import scala.util.{Failure, Success, Try}
import wizard.actionmanagement.InputRouter

class Human private[player](name: String) extends Player(name) {
    
    override def bid(): Int = {
        def loop(): Int = {
            val line = InputRouter.readLine()
            line.toIntOption match {
                case Some(n) => n
                case None => loop()
            }
        }
        loop()
    }
    
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = {
        def loop(): Card = {
            val line = InputRouter.readLine()
            val idx = line.toIntOption.getOrElse(0) - 1
            val cards = hand.cards
            if (idx >= 0 && idx < cards.length) cards(idx) else loop()
        }
        loop()
    }
    
    // Backward-compatible overload used in some tests
    def playCard(leadColor: Color, trump: Color, currentPlayerIndex: Int): Card =
        playCard(Option(leadColor), Option(trump), currentPlayerIndex)
}

// Companion object
//factory method
object Human {
    def create(name: String): Try[Human] = {
        if (name.isEmpty) Failure(new IllegalArgumentException("Name must not be empty"))
        else Success(new Human(name))
    }
}
