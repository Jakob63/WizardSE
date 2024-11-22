package wizard

import org.scalatest.Sequential

class aTestSequence extends Sequential (new PlayerTest(), new RoundLogicTest(), new HandTest(), new RoundTest(), new CardTests(), new PlayerLogicTest(), new GameTest(), new WizardTests(), new PlayerLogicTests(), new DealerTests(), new TextUITest(), new GameLogicTest()) {}

