package wizard.components

import wizard.aView.TextUI
import wizard.actionmanagement.Observer

class DefaultConfiguration extends Configuration {
  override def observables: Set[Observer] = Set[Observer](TextUI)
}
