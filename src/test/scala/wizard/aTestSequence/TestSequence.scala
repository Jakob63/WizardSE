package wizard.aTestSequence

import org.scalatest.Sequential
import wizard.WizardTests
import wizard.aView.{TextUITest, TextUIBidirectionalUndoTest, TextUINameInputCancellationTest, TextUIQuietOnEmptyCountInputTest}
import wizard.aView.aView_GUI.{WizardGUITest, WizardGUIBackToCountTest, WizardGUIReuseContainerTest, WizardGUIUndoFromNamesTest, WizardGUINameSwitchCancellationTest, WizardGUILocalBackImmediateTest}
import wizard.actionmanagement.ObserverTests
import wizard.controller.{GameLogicTest, NormalCardStateTest, PlayerLogicTest, PlayerLogicTests, RoundLogicTest, StateTests, GameLogicResetPlayerCountTest}
import wizard.model.*
import wizard.model.dealer.DealerTests
import wizard.model.player.{AI_Test, BuildHumanTest, HumanTest, PlayerTest}
import wizard.model.rounds.RoundTest
import wizard.undo.UndoManagerTest

class TestSequence extends Sequential (
  new PlayerTest(),
  new RoundLogicTest(),
  new HandTest(),
  new RoundTest(),
  new CardTests(),
  new PlayerLogicTest(),
  new GameTest(),
  new WizardTests(),
  new PlayerLogicTests(),
  new DealerTests(),
  new TextUITest(),
  new GameLogicTest(),
  new ObserverTests,
  new UndoManagerTest(),
  new AI_Test(),
  new StateTests(),
  new BuildHumanTest(),
  new NormalCardStateTest(),
  new HumanTest(),
  // Newly added GUI tests
  new WizardGUITest(),
  new WizardGUIBackToCountTest(),
  // Controller regression test for player count reset idempotency
  new GameLogicResetPlayerCountTest(),
  new TextUIQuietOnEmptyCountInputTest(),
  // New controller idempotent selection test
  new wizard.controller.GameLogicIdempotentPlayerCountTest()
) {}

