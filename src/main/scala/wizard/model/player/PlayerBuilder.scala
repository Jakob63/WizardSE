package wizard.model.player

trait PlayerBuilder {
    def setName(name: String): PlayerBuilder
    def reset(): PlayerBuilder
    def build(): Player
}