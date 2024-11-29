package wizard.model.player

import wizard.model.cards.{Card, Color}

class BuildAI extends PlayerBuilder {

    private var unfinished: Option[AI] = None
    
    override def setName(name: String): PlayerBuilder = {
        if (unfinished.isEmpty) {
            unfinished = Some(AI(name))
        } else {
            unfinished.get.name = name
        }
        this
    }

    override def reset(): PlayerBuilder = {
        unfinished = None
        this
    }

    override def build(): Player = {
        if (unfinished.isDefined) {
            var player = unfinished.get
            reset()
            return player
        }
        throw new Exception("AI not built yet")
    }
}
