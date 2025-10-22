package wizard.aView

import wizard.controller.aGameLogic

trait View {
  def init(gameLogic: aGameLogic): Unit
}
