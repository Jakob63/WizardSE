package wizard.aTestSequence

import org.scalatest.Sequential
import wizard.WizardTests
import wizard.aView.TextUITest
import wizard.actionmanagement.ObserverTests
import wizard.controller.{GameLogicTest, PlayerLogicTest, PlayerLogicTests, RoundLogicTest}
import wizard.model.*
import wizard.model.player.PlayerTest
import wizard.model.rounds.RoundTest

class TestSequence extends Sequential (new PlayerTest(), new RoundLogicTest(), new HandTest(), new RoundTest(), new CardTests(), new PlayerLogicTest(), new GameTest(), new WizardTests(), new PlayerLogicTests(), new DealerTests(), new TextUITest(), new GameLogicTest(), new ObserverTests) {}

