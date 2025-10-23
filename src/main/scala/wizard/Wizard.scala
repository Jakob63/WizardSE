package wizard


import util.UserInput
import wizard.aView.TextUI
import wizard.components.{Configuration, DefaultConfig}
import wizard.controller.controllerBaseImpl.{BaseGameLogic, BasePlayerLogic, BaseRoundLogic}
import wizard.controller.{GameLogic, PlayerLogic, RoundLogic}

object Wizard {

  class Wizard {

  }

  def entry(config: Configuration) = {
    val gameLogic = BaseGameLogic()
    val playerLogic = BasePlayerLogic()
    val roundLogic = BaseRoundLogic()

    gameLogic.roundLogic = roundLogic
    roundLogic.playerLogic = playerLogic
    roundLogic.gameLogic = gameLogic
    playerLogic.gameLogic = gameLogic
    for (view <- config.views) {
      view.init(gameLogic)
    }
    for (observer <- config.observables) {
      gameLogic.add(observer)
      playerLogic.add(observer)
      roundLogic.add(observer)
    }
    gameLogic.startGame()
  }

  // weitere entry Methode um sie zu überladen und userInput durchzureichen
  def entry(config: Configuration, input: UserInput): Unit = {
    val gameLogic = BaseGameLogic()
    val playerLogic = BasePlayerLogic()
    val roundLogic = BaseRoundLogic()

    // Logik verknüpfen
    gameLogic.roundLogic = roundLogic
    roundLogic.playerLogic = playerLogic
    roundLogic.gameLogic = gameLogic
    playerLogic.gameLogic = gameLogic

    // UserInput an alle Bedarfsträger verteilen
    playerLogic.userInput = input
    wizard.aView.TextUI.userInput = input

    // Views/Observer verdrahten
    for (view <- config.views) view.init(gameLogic)
    for (observer <- config.observables) {
      gameLogic.add(observer)
      playerLogic.add(observer)
      roundLogic.add(observer)
    }

    gameLogic.startGame()
  }

  def main(args: Array[String]): Unit = {
    val config = DefaultConfig()
    entry(config)

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