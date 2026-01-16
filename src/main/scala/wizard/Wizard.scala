package wizard

import wizard.aView.TextUI
import wizard.controller.GameLogic
import wizard.aView.aView_GUI.WizardGUI
import java.util.logging.{Level, Logger}
import wizard.actionmanagement.Debug

object Wizard {
    val eol = sys.props("line.separator")
    def bar(cellWidth: Int = 4, cellNum: Int = 2) =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def bar2(cellWidth: Int = 16, cellNum: Int = 2) =
        (("-" * cellWidth) * cellNum) + "-" + eol

    def cells(cellWidth: Int = 7, cellNum: Int = 1) =
        ("|" + " " * cellWidth) * cellNum + "|" + eol

    def cells2() =
        "|" + " game  " + "|" + eol

    def cells3() =
        "|" + " trump " + "|" + eol

    def cells4() =
        ("|" + "Set win" + "|" + "\t") * 3 + eol

    def cells5(cellWidth: Int = 7, cellNum: Int = 1) =
        (("|" + " " * cellWidth) * cellNum + "|" + "\t") * 3 + eol

    def mesh2: String =
        bar() + cells() + cells2() + cells() + bar()

    def mesh3: String =
        bar() + cells() + cells3() + cells() + bar()

    def mesh4: String =
        bar2() + cells5() + cells4() + cells5() + bar2()

    def main(args: Array[String]): Unit = {
        try {
            System.setProperty("javafx.logging.level", "OFF")
        } catch {
            case _: Throwable => ()
        }
        try {
            val loggers = List(
                "javafx",
                "com.sun.javafx",
                "com.sun.javafx.application"
            )
            loggers.foreach { name =>
                val l = Logger.getLogger(name)
                l.setUseParentHandlers(false)
                l.setLevel(Level.OFF)
            }
        } catch {
            case _: Throwable => ()
        }
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