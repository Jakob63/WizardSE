package wizard

import wizard.aView.TextUI
import wizard.controller.GameLogic
import wizard.aView.aView_GUI.WizardGUI
import wizard.actionmanagement.Debug

object Wizard {
    val eol: String = sys.props("line.separator")
    def bar(cellWidth: Int = 4, cellNum: Int = 2): String =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def bar2(cellWidth: Int = 16, cellNum: Int = 2): String =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def cells(cellWidth: Int = 7, cellNum: Int = 1): String =
        ("|" * 1 + " " * cellWidth) * cellNum + "|" + eol

    def cells2(): String =
        "|" + " game  " + "|" + eol

    def cells3(): String =
        "|" + " trump " + "|" + eol

    def cells4(): String =
        ("|" + "Set win" + "|" + "\t") * 3 + eol

    def cells5(cellWidth: Int = 7, cellNum: Int = 1): String =
        (("|" + " " * cellWidth) * cellNum + "|" + "\t") * 3 + eol

    def mesh2: String =
        bar() + cells() + cells2() + cells() + bar()

    def mesh3: String =
        bar() + cells() + cells3() + cells() + bar()

    def mesh4: String =
        bar2() + cells5() + cells4() + cells5() + bar2()

    def main(args: Array[String]): Unit = {
      Debug.enabled = false // hier für Debug Logs auf true setzen
      Debug.initEnvironment()
        try { System.setProperty("WIZARD_INTERACTIVE", "1") } catch { case _: Throwable => () }
        val controlG = new GameLogic
        Debug.log("Wizard.main -> created GameLogic controller")
        val tui = new TextUI(controlG)
        Debug.log("Wizard.main -> created TextUI and registered as observer")
        val gui = new WizardGUI(controlG)
        Debug.log("Wizard.main -> created WizardGUI")

        gui.main(args)
    }
}