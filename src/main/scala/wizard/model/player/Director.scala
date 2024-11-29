package wizard.model.player

import scala.util.Random

object Director {
    private var builder: PlayerBuilder = _

    var names = List("Manfred", "Olaf", "Siegfried", "Irmie", "Adam", "Eva", "Gustav", "Hans", "Gisela", "Gudrun", "Hildegard", "Inge", "Karl", "Ludwig")
    
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
