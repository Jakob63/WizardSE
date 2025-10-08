package wizard.aView.aView_GUI

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{VBox, StackPane}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import scalafx.application.Platform
import javafx.beans.binding.Bindings
import wizard.controller.GameLogic
import wizard.actionmanagement.Observer
import wizard.model.player.{PlayerFactory, PlayerType}

class WizardGUI(val gameController: GameLogic) extends JFXApp3 with Observer {
  // Buffer to handle events arriving before the JavaFX Stage is ready
  private var pendingPlayerCount: Option[Int] = None
  // Reference to the current content container so we can swap screens without replacing the root
  private var contentBox: Option[VBox] = None
  // Unified style for all text input fields (dark gray like the player count box)
  private val inputFieldStyle: String = "-fx-control-inner-background: #2B2B2B; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6);"
  // Unified style for primary buttons in the UI (dark gray background like inputs)
  private val buttonStyle: String = "-fx-background-color: #2B2B2B; -fx-text-fill: white;"
  // self-register as observer
  gameController.add(this)
  println("[DEBUG_LOG] WizardGUI constructed and registered as observer")

  override def start(): Unit = {
    println("[DEBUG_LOG] WizardGUI.start initializing stage")
    stage = new JFXApp3.PrimaryStage {
      title = "Wizard Card Game"
      width = 600
      height = 400
      scene = new Scene {
        root = createInitialScreen()
      }
    }
    // Ensure we are definitely registered (in case the instance was created before add or re-created by JavaFX)
    gameController.add(this)
    val subsNow = try gameController.subscribers.map(_.getClass.getName).mkString("[", ", ", "]") catch { case _: Throwable => "[]" }
    println(s"[DEBUG_LOG] WizardGUI.start stage ready=${stage != null}, scene ready=${stage != null && stage.scene() != null}, contentBoxDefined=${contentBox.isDefined}, controllerSubscribers=$subsNow")
    // Start the controller now that the GUI is ready (on a background daemon thread)
    val controllerThread = new Thread(new Runnable { override def run(): Unit = gameController.start() })
    controllerThread.setDaemon(true)
    controllerThread.start()
    // If an early PlayerCountSelected arrived before the stage was ready, apply it now
    pendingPlayerCount.foreach { cnt =>
      println(s"[DEBUG_LOG] WizardGUI.start applying buffered PlayerCountSelected($cnt)")
      Platform.runLater {
        val stageReady = stage != null && stage.scene() != null
        println(s"[DEBUG_LOG] WizardGUI.start.runLater stageReady=$stageReady, contentBoxDefined=${contentBox.isDefined}")
        if (stageReady) {
          contentBox match {
            case Some(box) =>
              println("[DEBUG_LOG] WizardGUI.start swapping VBox children to name-entry screen")
              box.children = createPlayerNameScreen(cnt)
            case None =>
              println("[DEBUG_LOG] WizardGUI.start replacing root with name-entry root")
              stage.scene().root = createPlayerNameRoot(cnt)
          }
          pendingPlayerCount = None
        }
      }
    }
  }

  private def createInitialScreen(): StackPane = {
    val titleLabel = new Label("Willkommen bei Wizard") {
      font = Font.font(null, FontWeight.Bold, 24)
      style = "-fx-text-fill: #39FF14;" // Neon green
    }

    val playerCountLabel = new Label("Spieleranzahl (3-6)") {
      font = Font.font(20)
      style = "-fx-text-fill: black;"
      translateY = -12
    }

    val playerCountField = new TextField() {
      maxWidth = 100
      alignment = Pos.Center
      style = inputFieldStyle
    }

    val nextButton = new Button("Weiter") {
      minWidth = 100
      style = buttonStyle
    }

    // UI-Container (Vordergrund)
    val ui = new VBox(20) {
      alignment = Pos.Center
      padding = Insets(20)
      children = List(titleLabel, playerCountLabel, playerCountField, nextButton)
    }
    // Keep a reference so we can switch content when TUI drives the flow
    contentBox = Some(ui)

    // Hintergrundbild laden (aus Ressourcen)
    val bgUrl = Option(getClass.getResource("/images/Wizard_game_background2_GUI.png")).getOrElse(
      throw new IllegalArgumentException("Hintergrundbild /images/Wizard_game_background2_GUI.png nicht gefunden – bitte unter src/main/resources/images/ ablegen.")
    )
    val bgView = new ImageView(new Image(bgUrl.toExternalForm)) {
      preserveRatio = false // echte Streckung in beide Dimensionen
    }

    // Root-Container mit Bild im Hintergrund und UI im Vordergrund
    val root = new StackPane {
      children = Seq(bgView, ui)
    }

    // Hintergrund an Root-Größe binden (exaktes Mitstrecken)
    bgView.fitWidth  <== root.width
    bgView.fitHeight <== root.height

    // UI-Container responsiv an Root binden
    ui.prefWidth <== root.width * 0.8
    ui.maxWidth  <== root.width * 0.8
    ui.spacing   <== root.height * 0.03

    // Controls responsiv binden (Breite)
    playerCountField.prefWidth <== ui.width * 0.3
    nextButton.prefWidth       <== ui.width * 0.3
    playerCountField.maxWidth  <== playerCountField.prefWidth
    nextButton.maxWidth        <== nextButton.prefWidth

    // Fonts dynamisch skalieren über CSS-Style
    titleLabel.style <== Bindings.concat("-fx-text-fill: #39FF14; -fx-font-weight: bold; -fx-font-size: ", root.width / 25, "px;")
    playerCountLabel.style <== Bindings.concat("-fx-text-fill: black; -fx-font-size: ", root.width / 35, "px;")
    playerCountField.style <== Bindings.concat(inputFieldStyle, " -fx-font-size: ", root.width / 40, "px;")
    nextButton.style <== Bindings.concat(buttonStyle, " -fx-font-size: ", root.width / 40, "px;")

    // Button-Logik
    nextButton.onAction = _ => {
      val playerCount = playerCountField.text.value
      if (playerCount.matches("[3-6]")) {
        // Notify controller so other views (e.g., TUI) can sync
        gameController.playerCountSelected(playerCount.toInt)
        ui.children = createPlayerNameScreen(playerCount.toInt)
      }
    }

    root
  }

  private def createPlayerNameRoot(playerCount: Int): StackPane = {
    val ui = new VBox(20) {
      alignment = Pos.Center
      padding = Insets(20)
    }

    // make this UI the current content for responsive bindings BEFORE creating children
    contentBox = Some(ui)
    ui.children = createPlayerNameScreen(playerCount)

    val bgUrl = Option(getClass.getResource("/images/Wizard_game_background2_GUI.png")).getOrElse(
      throw new IllegalArgumentException("Hintergrundbild /images/Wizard_game_background2_GUI.png nicht gefunden – bitte unter src/main/resources/images/ ablegen.")
    )
    val bgView = new ImageView(new Image(bgUrl.toExternalForm)) {
      preserveRatio = false
    }

    val root = new StackPane {
      children = Seq(bgView, ui)
    }
    bgView.fitWidth <== root.width
    bgView.fitHeight <== root.height

    // Responsive bindings for this screen
    ui.prefWidth <== root.width * 0.8
    ui.maxWidth  <== root.width * 0.8
    ui.spacing   <== root.height * 0.03

    root
  }

  private def createPlayerNameScreen(playerCount: Int): List[scalafx.scene.Node] = {
    val titleLabel = new Label("Spielernamen:") {
      font = Font.font(null, FontWeight.Bold, 20)
    }

    val playerFields = (1 to playerCount).map { i =>
      new TextField() {
        promptText = s"Spieler $i"
        style = inputFieldStyle
      }
    }.toList

    val startButton = new Button("START") {
      style = buttonStyle
    }

    // Responsives Verhalten relativ zum aktuellen Content-Container (falls vorhanden)
    contentBox.foreach { box =>
      // Container-Spaltmaß und Breitenbindung
      box.spacing <== box.height * 0.03
      val compWidth = box.width * 0.35
      playerFields.foreach { tf =>
        tf.prefWidth <== compWidth
        tf.maxWidth  <== tf.prefWidth
      }
      startButton.prefWidth <== compWidth
      startButton.maxWidth  <== startButton.prefWidth

      // Dynamische Schriftgrößen
      titleLabel.style <== Bindings.concat("-fx-font-weight: bold; -fx-font-size: ", box.width / 30, "px;")
      playerFields.foreach(_.style <== Bindings.concat(inputFieldStyle, " -fx-font-size: ", box.width / 40, "px;"))
      startButton.style <== Bindings.concat(buttonStyle, " -fx-font-size: ", box.width / 40, "px;")
    }

    startButton.onAction = _ => {
      val playerNames = playerFields.map(_.text.value.trim).filter(_.nonEmpty)
      if (playerNames.length == playerCount) {
        val players = playerNames.map(name => PlayerFactory.createPlayer(Some(name), PlayerType.Human))
        gameController.setPlayers(players)
      }
    }

    List(titleLabel) ++ playerFields :+ startButton
  }

  // Minimal observer implementation to keep GUI and TUI in sync via events
  override def update(updateMSG: String, obj: Any*): Any = {
    updateMSG match {
      case "PlayerCountSelected" =>
        val count = obj.head match {
          case pcs: wizard.actionmanagement.PlayerCountSelected => pcs.count
          case i: Int => i
          case _ => 0
        }
        println("[DEBUG_LOG] GUI received PlayerCountSelected(" + count + ")")
        if (count >= 3 && count <= 6) {
          // If stage/scene not ready yet, buffer the desired state change
          if (stage == null || stage.scene() == null) {
            println("[DEBUG_LOG] GUI buffering PlayerCountSelected because stage/scene not ready")
            pendingPlayerCount = Some(count)
          } else {
            println(s"[DEBUG_LOG] GUI applying PlayerCountSelected immediately, contentBoxDefined=${contentBox.isDefined}")
            Platform.runLater {
              contentBox match {
                case Some(box) =>
                  println("[DEBUG_LOG] GUI swapping VBox children to name-entry screen")
                  box.children = createPlayerNameScreen(count)
                case None =>
                  if (stage != null && stage.scene() != null) {
                    println("[DEBUG_LOG] GUI replacing root with name-entry root")
                    stage.scene().root = createPlayerNameRoot(count)
                  }
              }
            }
          }
        } else {
          println(s"[DEBUG_LOG] GUI ignored PlayerCountSelected($count) because count out of range")
        }
        ()
      case "StartGame" => () // could switch scene to input players
      case "CardsDealt" => () // update GUI hand display
      case "ShowHand" => () // render specific player's hand
      case _ => ()
    }
  }
}

object WizardGUI {
  // Fallback launcher creating its own controller if started standalone
  def main(args: Array[String]): Unit = {
    val controller = new GameLogic
    val app = new WizardGUI(controller)
    app.main(args)
  }
}