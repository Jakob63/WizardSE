package wizard.aTestSequence

import org.scalatest.Sequential
import wizard.WizardTests
import wizard.aView.TextUITest
import wizard.actionmanagement.ObserverTests
import wizard.controller.{GameLogicTest, NormalCardStateTest, PlayerLogicTest, PlayerLogicTests, RoundLogicTest, StateTests}
import wizard.model.*
import wizard.model.dealer.DealerTests
import wizard.model.player.{AI_Test, BuildHumanTest, HumanTest, PlayerTest}
import wizard.model.rounds.RoundTest
import wizard.undo.UndoManagerTest

class TestSequence extends Sequential (new PlayerTest(), new RoundLogicTest(), new HandTest(), new RoundTest(), new CardTests(), new PlayerLogicTest(), new GameTest(), new WizardTests(), new PlayerLogicTests(), new DealerTests(), new TextUITest(), new GameLogicTest(), new ObserverTests, new UndoManagerTest(), new AI_Test(), new StateTests(), new BuildHumanTest(), new NormalCardStateTest(), new HumanTest()) {}

