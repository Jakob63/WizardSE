package wizard.pattern.card

import wizard.model.cards.{Card, Color, Value}

object CardFactory {
    def createCard(value: Value, color: Color): Card = {
        Card(value, color)
    }
}