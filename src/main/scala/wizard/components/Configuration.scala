package wizard.components

import wizard.aView.View
import wizard.actionmanagement.Observer

trait Configuration {
  def observables: Set[Observer]
  def views: Set[View]
}
