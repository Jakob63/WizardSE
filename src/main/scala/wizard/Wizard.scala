package wizard

import wizard.aView.TextUI
import wizard.controller.GameLogic
import wizard.aView.aView_GUI.WizardGUI
import java.util.logging.{Level, Logger}

object Wizard {
    class Wizard {

    }

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
        // Suppress JavaFX startup warning by configuring logging before any JavaFX class loads
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
        val controlG = new GameLogic
        val tui = new TextUI(controlG)
        val gui = new WizardGUI(controlG) // GUI registers as observer in its constructor

        // Controller will be started from WizardGUI.start after the stage is ready
        // Launch the ScalaFX application so the GUI window appears
        gui.main(args)
    }
}