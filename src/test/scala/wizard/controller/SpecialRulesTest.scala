package wizard.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import wizard.model.player.Player
import wizard.model.cards.{Card, Color, Value}
import wizard.model.rounds.Round
import wizard.actionmanagement.InputRouter

class SpecialRulesTest extends AnyWordSpec with Matchers {

  class TestPlayer(name: String) extends Player(name) {
    override def bid(): Int = 0
    override def playCard(leadColor: Option[Color], trump: Option[Color], currentPlayerIndex: Int): Card = 
      Card(Value.One, Color.Red)
  }

  "RoundLogic Special Rules" should {
    val roundLogic = new RoundLogic
    val p1 = new TestPlayer("P1")
    val p2 = new TestPlayer("P2")
    val p3 = new TestPlayer("P3")
    val players = List(p1, p2, p3)
    val round = new Round(players)

    "correctly determine the winner when only Jesters (Chester) are played" in {
      val trick = List(
        (p1, Card(Value.Chester, Color.Red)),
        (p2, Card(Value.Chester, Color.Blue)),
        (p3, Card(Value.Chester, Color.Green))
      )
      roundLogic.trickwinner(trick, round) should be(p1)
    }

    "handle Wizard as trump card by allowing player to choose trump color" in {
      val wizardState = new WizardCardState
      val trumpCard = Card(Value.WizardKarte, Color.Yellow)
      
      InputRouter.clear()
      InputRouter.offer("4")
      wizardState.handleTrump(round, trumpCard, players)
      
      round.trump should be(Some(Color.Yellow))
    }
    
    "handle Wizard as trump card with invalid input by defaulting to Red" in {
      val wizardState = new WizardCardState
      val trumpCard = Card(Value.WizardKarte, Color.Yellow)
      
      InputRouter.clear()
      InputRouter.offer("invalid")
      wizardState.handleTrump(round, trumpCard, players)
      
      round.trump should be(Some(Color.Red))
    }

    "correctly determine winner when only Jesters are played (extended)" in {
      round.setTrump(None)
      round.leadColor = None
      val trick = List(
        (p1, Card(Value.Chester, Color.Red)),
        (p2, Card(Value.Chester, Color.Blue))
      )
      roundLogic.trickwinner(trick, round) should be(p1)
    }

    "handle Wizard trump choosing with out of bounds index" in {
      val wizardState = new WizardCardState
      val trumpCard = Card(Value.WizardKarte, Color.Yellow)
      
      InputRouter.clear()
      InputRouter.offer("10")
      wizardState.handleTrump(round, trumpCard, players)
      
      round.trump should be(Some(Color.Red))
    }

    "correctly determine winner when trick starts with multiple Jesters followed by a normal card" in {
      round.setTrump(Some(Color.Green))
      round.leadColor = None
      
      val trick = List(
        (p1, Card(Value.Chester, Color.Red)),
        (p2, Card(Value.Chester, Color.Blue)),
        (p3, Card(Value.Seven, Color.Yellow))
      )
      
      roundLogic.trickwinner(trick, round) should be(p3)
    }
    
    "correctly determine winner when trick has Jesters, Trump and Wizard" in {
      round.setTrump(Some(Color.Green))
      
      val trick = List(
        (p1, Card(Value.Chester, Color.Red)),
        (p2, Card(Value.Seven, Color.Green)),
        (p3, Card(Value.WizardKarte, Color.Blue))
      )
      
      roundLogic.trickwinner(trick, round) should be(p3)
    }
  }
}
