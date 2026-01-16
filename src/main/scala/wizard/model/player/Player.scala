package wizard.model.player

import wizard.model.cards.{Card, Color, Hand}
import wizard.actionmanagement.{Observable, Observer}
import wizard.aView.TextUI

abstract case class Player(var name: String) extends Observable {
    
    var hand: Hand = Hand(List[Card]())

    var points: Int = 0

    var tricks: Int = 0

    var bids: Int = 0

    var roundPoints: Int = 0

    var roundBids: Int = 0

    var roundTricks: Int = 0

    def addHand(hand: Hand): Unit = {
        this.hand = hand
    }

    def addTricks(tricks: Int): Unit = {
        this.tricks += tricks
    }
    def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card
    def bid(): Int
}
