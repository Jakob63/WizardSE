package wizard.components

import wizard.actionmanagement.Observer

trait Configuration {
  def observables: Set[Observer]
}