package wizard.model.player

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.cards.{Card, Color, Hand, Value}
import wizard.actionmanagement.InputRouter

class HumanTest extends AnyWordSpec with Matchers {

  "A Human" should {
    
    "be created with a name" in {
      val human = new Human("Alice")
      human.name should be("Alice")
    }

    "bid correctly based on input" in {
      val human = new Human("Alice")
      InputRouter.clear()
      InputRouter.offer("3")
      
      human.bid() should be(3)
    }

    "retry bidding on invalid input" in {
      val human = new Human("Alice")
      InputRouter.clear()
      InputRouter.offer("abc")
      InputRouter.offer("5")
      
      human.bid() should be(5)
    }

    "throw GameStoppedException when bidding is stopped" in {
      val human = new Human("Alice")
      InputRouter.clear()
      InputRouter.offer("__GAME_STOPPED__")
      
      intercept[wizard.actionmanagement.GameStoppedException] {
        human.bid()
      }
    }

    "play a card correctly based on input" in {
      val human = new Human("Alice")
      val card1 = Card(Value.One, Color.Red)
      val card2 = Card(Value.Two, Color.Blue)
      human.hand = Hand(List(card1, card2))
      
      InputRouter.clear()
      InputRouter.offer("2")
      
      human.playCard(None, None, 0) should be(card2)
    }

    "retry playing card on invalid index" in {
      val human = new Human("Alice")
      val card1 = Card(Value.One, Color.Red)
      human.hand = Hand(List(card1))
      
      InputRouter.clear()
      InputRouter.offer("5")
      InputRouter.offer("xyz")
      InputRouter.offer("1")
      
      human.playCard(None, None, 0) should be(card1)
    }

    "throw GameStoppedException when playing card is stopped" in {
      val human = new Human("Alice")
      InputRouter.clear()
      InputRouter.offer("__GAME_STOPPED__")
      
      intercept[wizard.actionmanagement.GameStoppedException] {
        human.playCard(None, None, 0)
      }
    }

    "have a companion object factory" in {
      Human.create("Bob").isSuccess should be(true)
      Human.create("Bob").get.name should be("Bob")
      Human.create("").isFailure should be(true)
    }

    "support backward compatible playCard" in {
        val human = new Human("Alice")
        val card = Card(Value.Three, Color.Green)
        human.hand = Hand(List(card))
        InputRouter.clear()
        InputRouter.offer("1")
        human.playCard(Color.Red, Color.Blue, 0) should be(card)
    }
  }
}
