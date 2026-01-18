package wizard.model.cards

import scala.collection.mutable.ListBuffer
import wizard.actionmanagement.Observable

object Dealer extends Observable {

    var allCards: List[Card] = {
        val buffer = ListBuffer[Card]()
        for {
            color <- Color.values.toList
            value <- Value.values.toList
        } buffer += Card(value, color)
        val initial = buffer.toList
        wizard.actionmanagement.Debug.log(s"Dealer.allCards initialization (deterministic) -> first card: ${initial.headOption}")
        initial
    }
    var index = 0

    def shuffleCards(): Boolean = {
        index = 0
        val isInteractive = {
            val prop = sys.props.get("WIZARD_INTERACTIVE").exists(v => v != "0" && v.toLowerCase != "false")
            prop || (System.console() != null && sys.env.get("GITHUB_ACTIONS").isEmpty)
        }
        if (isInteractive) {
            allCards = scala.util.Random().shuffle(allCards)
        }
        wizard.actionmanagement.Debug.log(s"Dealer.shuffleCards (interactive=$isInteractive) -> first card: ${allCards.headOption}")
        true
    }
    def dealCards(cards_amount: Int, excludeCard: Option[Card] = None): Hand = {
        val listbuffer = ListBuffer[Card]()
        for (i <- 1 to cards_amount) {
            var card = allCards(index)
            while (excludeCard.contains(card)) {
                index += 1
                card = allCards(index)
            }
            listbuffer.addOne(card)
            index += 1
        }
        Hand(listbuffer.toList)
    }
}
