package wizard.aView.aView_GUI

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes.*
import scalafx.application.Platform
import javafx.beans.binding.Bindings
import wizard.aView.UI
import wizard.controller.GameLogic
import wizard.actionmanagement.Observer
import wizard.model.player.{PlayerFactory, PlayerType}

import scala.compiletime.uninitialized

class WizardGUI() extends JFXApp3 with Observer with UI {

  private var gameController: GameLogic = uninitialized

  override def initialize(gameLogic: GameLogic): Unit = {
    gameController = gameLogic
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        // Launch the ScalaFX/JavaFX application for this instance on a background thread
        WizardGUI.this.main(Array.empty[String])
      }
    })
    // Nicht als Demon Thread markieren, sonst beendet sich die JVM isntant
    // thread.setDaemon(true)
    thread.start()
  }

  // Buffer to handle events arriving before the JavaFX Stage is ready
  private var pendingPlayerCount: Option[Int] = None
  // Reference to the current content container so we can swap screens without replacing the root
  private var contentBox: Option[VBox] = None
  // Unified style for all text input fields (dark gray like the player count box)
  private val inputFieldStyle: String = "-fx-control-inner-background: #2B2B2B; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6);"
  // Unified style for primary buttons in the UI (dark gray background like inputs)
  private val buttonStyle: String = "-fx-background-color: #2B2B2B; -fx-text-fill: white;"
  // self-register as observer

  override def start(): Unit = {
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
    // Start the controller now that the GUI is ready (on a background daemon thread)
    val controllerThread = new Thread(new Runnable { override def run(): Unit = gameController.start() })
    controllerThread.setDaemon(true)
    controllerThread.start()
    // If an early PlayerCountSelected arrived before the stage was ready, apply it now
    pendingPlayerCount.foreach { cnt =>
      Platform.runLater {
        val stageReady = stage != null && stage.scene() != null
        if (stageReady) {
          contentBox match {
            case Some(box) =>
              box.children = createPlayerNameScreen(cnt)
            case None =>
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
        val cnt = playerCount.toInt
        // Controller-Event auf Hintergrund-Thread verlagern, damit TUI mit readLine nicht FX-Thread blockiert
        new Thread(() => gameController.playerCountSelected(cnt)).start()
        // Lokalen UI-Wechsel direkt (weiterhin auf dem FX-Thread) durchführen
        // Notify controller so other views (e.g., TUI) can sync

        // gameController.playerCountSelected(playerCount.toInt)
        // ui.children = createPlayerNameScreen(playerCount.toInt)
        // dopplung weil eigentlich über Observer Pattern läuft: ui.children = createPlayerNameScreen(cnt)
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
        // auskommentiert, damit Controller Aktionen nicht auf dem FX-Thread ausgeführt werden
        // gameController.setPlayers(players)
        new Thread(() => gameController.setPlayers(players)).start()
        // Optional für sofotiges Feedback auf der GUI (debugging)
        // startButton.disable = true
        // oder Status-Label-Meldung setzen
        // Aber das ist nur Good to know wissen, falls man was debuggen möchte
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
        if (count >= 3 && count <= 6) {
          // If stage/scene not ready yet, buffer the desired state change
          if (stage == null || stage.scene() == null) {
            pendingPlayerCount = Some(count)
          } else {
            Platform.runLater {
              contentBox match {
                case Some(box) =>
                  box.children = createPlayerNameScreen(count)
                case None =>
                  if (stage != null && stage.scene() != null) {
                    stage.scene().root = createPlayerNameRoot(count)
                  }
              }
            }
          }
        } else {
        }
        ()

      case "PlayersSet" =>
        Platform.runLater {
          contentBox.foreach { box =>
            box.children = List(new Label("Spiel startet... Karten werden ausgeteilt") {
              style = "-fx-text-fill: white; -fx-font-size: 18px;"
            })
          }
        }
        ()

      case "CardsDealt" =>
        Platform.runLater {
          // Erwartetes Payload: wizard.actionmanagement.CardsDealt(players: List[Player])
          val playersOpt: Option[List[wizard.model.player.Player]] = obj.headOption.flatMap {
            case cd: wizard.actionmanagement.CardsDealt => Some(cd.players)
            case ps: List[?] =>
              // Falls irgendwo eine Liste direkt geschickt wird (Fallback)
              ps.asInstanceOf[List[Any]] match {
                case list if list.forall(_.isInstanceOf[wizard.model.player.Player]) =>
                  Some(list.asInstanceOf[List[wizard.model.player.Player]])
                case _ => None
              }
            case _ => None
          }

          playersOpt.foreach { players =>
            // Root der Spielansicht: Titel + Karten je Spieler
            val title = new Label("Karten wurden ausgeteilt") {
              style = "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;"
            }

            // Eine Zeile pro Spieler: Name + Handkarten
            val playerRows: List[scalafx.scene.Node] = players.map { p =>
              val nameLbl = new Label(s"${p.name}") {
                style = "-fx-text-fill: #39FF14; -fx-font-size: 16px; -fx-font-weight: bold;"
              }

              // Karten als Textlabels (einfach): p.hand/toString je Karte
              val cardLabels: Seq[Label] = {
                val cards: List[Any] = try {
                  // Versuch, eine Handliste zu ermitteln. Passe diesen Zugriff ggf. an deine Player-API an.
                  // Häufig heißt es z. B. p.cards oder p.hand; hier defensiv via Reflection/Pattern.
                  val maybeCards =
                    try p.getClass.getMethod("cards").invoke(p)
                    catch { case _: Throwable => try p.getClass.getMethod("hand").invoke(p) catch { case _: Throwable => null } }
                  maybeCards match {
                    case xs: scala.collection.Iterable[?] => xs.toList.asInstanceOf[List[Any]]
                    case _ => Nil
                  }
                } catch { case _: Throwable => Nil }

                cards.zipWithIndex.map { case (c, idx) =>
                  new Label(c.toString) { // für ersten Wurf genügt toString
                    style = "-fx-background-color: #2B2B2B; -fx-text-fill: white; -fx-padding: 6 8; -fx-background-radius: 6;"
                  }
                }
              }

              val cardsRow = new scalafx.scene.layout.HBox(8) {
                alignment = Pos.CenterLeft
                children = cardLabels
              }

              new scalafx.scene.layout.VBox(6) {
                children = List(nameLbl, cardsRow)
              }
            }

            val gameView = new scalafx.scene.layout.VBox(16) {
              alignment = Pos.TopCenter
              padding = Insets(20)
              children = title :: playerRows
            }

            contentBox match {
              case Some(box) => box.children = List(gameView)
              case None => stage.scene().root = new StackPane { children = Seq(gameView) }
            }
          }
        }
        ()

      case "ShowHand" =>
        Platform.runLater {
          val playerOpt: Option[wizard.model.player.Player] = obj.headOption.flatMap {
            case sh: wizard.actionmanagement.ShowHand => Some(sh.player)
            case p: wizard.model.player.Player => Some(p)
            case _ => None
          }

          playerOpt.foreach { p =>
            val title = new Label(s"Hand von ${p.name}") {
              style = "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;"
            }

            val cardLabels: Seq[Label] = {
              val cards: List[Any] = try {
                val maybeCards =
                  try p.getClass.getMethod("cards").invoke(p)
                  catch { case _: Throwable => try p.getClass.getMethod("hand").invoke(p) catch { case _: Throwable => null } }
                maybeCards match {
                  case xs: scala.collection.Iterable[?] => xs.toList.asInstanceOf[List[Any]]
                  case _ => Nil
                }
              } catch { case _: Throwable => Nil }

              cards.map { c =>
                new Label(c.toString) {
                  style = "-fx-background-color: #2B2B2B; -fx-text-fill: white; -fx-padding: 6 8; -fx-background-radius: 6;"
                }
              }
            }

            val cardsRow = new scalafx.scene.layout.HBox(8) {
              alignment = Pos.Center
              children = cardLabels
            }

            val view = new scalafx.scene.layout.VBox(16) {
              alignment = Pos.TopCenter
              padding = Insets(20)
              children = List(title, cardsRow)
            }

            contentBox match {
              case Some(box) => box.children = List(view)
              case None => stage.scene().root = new StackPane { children = Seq(view) }
            }
          }
        }
        ()
      case "StartGame" => () // could switch scene to input players
      case _ => ()
    }
  }
}

object WizardGUI {
  // Fallback launcher creating its own controller if started standalone
  def standalone(args: Array[String]): Unit = {
    val controller = new GameLogic
    val app = new WizardGUI()
    app.initialize(controller)
  }
}