package wizard.aView

import wizard.controller.GameLogic

trait UI {
  def initialize(gameLogic: GameLogic): Unit
}
