package wizard.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerFactoryTest extends AnyWordSpec with Matchers {

  "BuildHuman" should {
    "build a Human player" in {
      val builder = new BuildHuman()
      val player = builder.setName("Human1").build()
      player shouldBe a[Human]
      player.name should be("Human1")
    }

    "allow resetting" in {
      val builder = new BuildHuman()
      builder.setName("Human1")
      builder.reset()
      intercept[Exception] {
        builder.build()
      }
    }
    
    "throw exception if build is called without name" in {
        val builder = new BuildHuman()
        intercept[Exception] {
            builder.build()
        }
    }
  }

  "BuildAI" should {
    "build an AI player" in {
      val builder = new BuildAI()
      val player = builder.setName("AI1").build()
      player shouldBe a[AI]
      player.name should be("AI1")
    }

    "allow resetting" in {
      val builder = new BuildAI()
      builder.setName("AI1")
      builder.reset()
      intercept[Exception] {
        builder.build()
      }
    }
  }

  "Director" should {
    "make a player with a specific name" in {
      val builder = new BuildHuman()
      val player = Director.makeWithName(builder, "DirectorPlayer")
      player.name should be("DirectorPlayer")
    }

    "make a player with a random name" in {
      val builder = new BuildHuman()
      val player = Director.makeRandomNames(builder)
      Director.names should contain (player.name)
    }
  }

  "PlayerFactory" should {
    "create a Human player" in {
      val player = PlayerFactory.createPlayer(Some("FactoryHuman"), PlayerType.Human)
      player shouldBe a[Human]
      player.name should be("FactoryHuman")
    }

    "create an AI player" in {
      val player = PlayerFactory.createPlayer(Some("FactoryAI"), PlayerType.AI)
      player shouldBe a[AI]
      player.name should be("FactoryAI")
    }

    "create a player with random name when name is None" in {
      val player = PlayerFactory.createPlayer(None, PlayerType.Human)
      Director.names should contain (player.name)
    }
  }
}
