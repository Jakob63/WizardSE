package wizard.components

import wizard.aView.UI
import wizard.actionmanagement.Observer

trait Configuration {
  def observables: Set[Observer]
  def uis: Set[UI]
}
