package wizard.components

import wizard.aView.aView_GUI.WizardGUI
import wizard.aView.{TextUI, UI}
import wizard.actionmanagement.Observer

class DefaultConfiguration extends Configuration {
  override def observables: Set[Observer] = Set[Observer]()

  override def uis: Set[UI] = Set[UI](TextUI(), WizardGUI())
}