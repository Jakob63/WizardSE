package wizard.components

import wizard.aView.{TextUI, View}
import wizard.actionmanagement.Observer

class DefaultConfig extends Configuration {
  override def observables: Set[Observer] = Set[Observer](TextUI)
  override def views: Set[View] = Set[View](TextUI)
}
