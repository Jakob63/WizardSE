package wizard.model.player

import scala.util.Random

object Director {
    private var builder: PlayerBuilder = _

    var names = List("Jakob", "Elena", "Janis", "Leon")
    
    def makeRandomNames(playerbuilder: PlayerBuilder): Player = {
        playerbuilder.reset()
        playerbuilder.setName(names(Random.nextInt(names.length)))
        playerbuilder.build()
    }
    
    def makeWithName(playerbuilder: PlayerBuilder, name: String): Player = {
        playerbuilder.reset()
        playerbuilder.setName(name)
        playerbuilder.build()
    }
}
