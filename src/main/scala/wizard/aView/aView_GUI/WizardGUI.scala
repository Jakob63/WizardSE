package wizard.aView.aView_GUI

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{VBox}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import wizard.controller.GameLogic

class WizardGUI extends JFXApp3 {
  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Wizard Card Game"
      width = 600
      height = 400
      scene = new Scene {
        root = createInitialScreen()
      }
    }
  }

  private def createInitialScreen(): VBox = {
    // Create UI elements for initial screen
    val titleLabel = new Label("Willkommen bei Wizard") {
      font = Font.font(null, FontWeight.Bold, 24)
    }

    val playerCountLabel = new Label("Spieleranzahl (3-6)") {
      font = Font.font(16)
    }

    val playerCountField = new TextField() {
      maxWidth = 100
      alignment = Pos.Center
    }

    val nextButton = new Button("Weiter") {
      minWidth = 100
    }

    // Create container for elements
    val container = new VBox(20) {
      alignment = Pos.Center
      padding = Insets(20)
      children = List(titleLabel, playerCountLabel, playerCountField, nextButton)
    }

    // Add button action
    nextButton.onAction = _ => {
      val playerCount = playerCountField.text.value
      if (playerCount.matches("[3-6]")) {
        container.children = createPlayerNameScreen(playerCount.toInt)
      }
    }

    container
  }

  private def createPlayerNameScreen(playerCount: Int): List[javafx.scene.Node] = {
    // Create UI elements for player name screen
    val titleLabel = new Label("Spielernamen:") {
      font = Font.font(null, FontWeight.Bold, 20)
    }

    val playerFields = (1 to playerCount).map { i =>
      new TextField() {
        promptText = s"Spieler $i"
        maxWidth = 200
      }
    }.toList

    val startButton = new Button("START") {
      minWidth = 100
    }

    // Add button action
    startButton.onAction = _ => {
      val playerNames = playerFields.map(_.text.value)
      if (playerNames.forall(_.nonEmpty)) {
        // Here you would start the game with the player names
        // This part needs to be implemented based on your game logic
        println("Starting game with players: " + playerNames.mkString(", "))
      }
    }

    // Return all elements
    List(titleLabel) ++ playerFields :+ startButton
  }
}

// Main object to launch the application
object WizardGUI {
  def main(args: Array[String]): Unit = {
    new WizardGUI().main(args)
  }
}


