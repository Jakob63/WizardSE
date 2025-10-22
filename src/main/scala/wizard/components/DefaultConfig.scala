package wizard.components

import wizard.aView.TextUI
import wizard.actionmanagement.Observer

class DefaultConfig extends Configuration {
  override def observables: Set[Observer] = Set[Observer](TextUI)
}