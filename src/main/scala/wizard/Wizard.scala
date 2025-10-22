package wizard



import wizard.aView.TextUI
import wizard.aView.aView_GUI.WizardGUI
import wizard.components.{Configuration, DefaultConfig}
import wizard.controller.controllerBaseImpl.{BaseGameLogic, BasePlayerLogic, BaseRoundLogic}
import wizard.controller.{aGameLogic, aRoundLogic, aPlayerLogic}

object Wizard {

  def entry(config: Configuration, launchGUI: Boolean = true): Unit = {
    // Controller-Instanzen erzeugen
    val gameLogic: aGameLogic = new BaseGameLogic()
    val roundLogic: aRoundLogic = new BaseRoundLogic()
    val playerLogic: aPlayerLogic = new wizard.controller.controllerBaseImpl.BasePlayerLogic()

    // Instanzen verdrahten
    gameLogic.asInstanceOf[BaseGameLogic].roundLogic = roundLogic
    roundLogic.asInstanceOf[BaseRoundLogic].playerLogic = playerLogic

    // Observer registrieren (TUI & GUI)
    TextUI.gameLogic = gameLogic

    // TUI registrieren (direkt oder über Config)
    for (observer <- config.observables) {
      gameLogic.asInstanceOf[BaseGameLogic].add(observer)
      playerLogic.asInstanceOf[BasePlayerLogic].add(observer)
      roundLogic.asInstanceOf[BaseRoundLogic].add(observer)
    }

    // GUI immer starten (ScalaFX App Thread)
    val guiThread = new Thread(() => {
      try {
        new WizardGUI(gameLogic).main(Array.empty)
      } catch {
        case_: Throwable
        => ()
      }
    })
    guiThread.setDaemon(true)
    guiThread.setName("WizardGUI-ScalaFX") // TODO: warum javaund nicht scala, was machen wenn ich nur scala darf
    guiThread.start()

    // trigger Spielstart
    gameLogic.startGame()


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


    //def mesh =
    //    (bar() + cells() * 3) + bar()

    def mesh2: String =
      bar() + cells() + cells2() + cells() + bar()

    def mesh3: String =
      bar() + cells() + cells3() + cells() + bar()

    def mesh4: String =
      bar2() + cells5() + cells4() + cells5() + bar2()

      println(mesh2)
      println(mesh3)
      println(mesh4)
  }

  def main(args: Array[String]): Unit = {
    val config = new DefaultConfig()
    entry(config)
  }
}