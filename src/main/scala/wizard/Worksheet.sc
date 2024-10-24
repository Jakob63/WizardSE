import wizard.cards.Dealer.shuffledCards
import wizard.cards.{Card, Color, Value}

val allCards: List[Card] = for {
    color <- Color.values.toList
    value <- Value.values.toList
} yield Card(value, color)
def shuffleCards(cards: List[Card]): List[Card] = {
    scala.util.Random.shuffle(cards)
}
val shuffledCards = shuffleCards(allCards)

println(shuffledCards.head.toString)
