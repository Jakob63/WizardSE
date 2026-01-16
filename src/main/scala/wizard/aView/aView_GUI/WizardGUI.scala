package wizard.aView.aView_GUI

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField, Tooltip, TableView, TableColumn}
import scalafx.beans.property.{StringProperty, IntegerProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{VBox, StackPane, HBox, BorderPane}
import scalafx.scene.layout.StackPane.setAlignment
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import scalafx.application.Platform
import javafx.beans.binding.Bindings
import wizard.controller.GameLogic
import wizard.actionmanagement.{Observer, Debug, InputRouter}
import wizard.model.player.{PlayerFactory, PlayerType, Player}
import wizard.model.cards.{Card, Color, Value}
import scalafx.scene.Node
import java.util.NoSuchElementException

class WizardGUI(val gameController: GameLogic) extends JFXApp3 with Observer {
  // keep reference to current root for overlay management
  private var rootPane: Option[StackPane] = None
  // top-left undo/redo toolbar (visible from player-name stage onwards)
  private var undoRedoBar: Option[HBox] = None
  // Buffer to handle events arriving before the JavaFX Stage is ready
  private var pendingPlayerCount: Option[Int] = None
  // Track selected player count for undo/back navigation
  private var selectedPlayerCount: Option[Int] = None
  // Reference to the current content container so we can swap screens without replacing the root
  private var contentBox: Option[VBox] = None
  // Track current screen for contextual undo handling: PlayerCount | PlayerNames | Game
  private var currentScreen: String = "PlayerCount"
  // Monotonic navigation epoch to cancel stale transitions scheduled via Platform.runLater
  private var navEpoch: Int = 0
  // Game table UI elements
  private var gameRoot: Option[BorderPane] = None
  private var trumpView: Option[ImageView] = None
  private var handBar: Option[HBox] = None
  private var bidOverlay: Option[HBox] = None
  // Players currently in the game (for scoreboard)
  private var currentPlayers: List[Player] = Nil
  private var scoresTable: Option[TableView[PlayerRow]] = None
  private var activePlayerName: Option[String] = None
  private var trickBar: Option[HBox] = None
  @volatile private var currentTrickCards: List[Card] = Nil

  case class PlayerRow(nameProp: String, bidProp: String, pointsProp: String) {
    val name = new StringProperty(this, "name", nameProp)
    val bid = new StringProperty(this, "bid", bidProp)
    val points = new StringProperty(this, "points", pointsProp)
  }
  // Unified style for all text input fields (dark gray like the player count box)
  private val inputFieldStyle: String = "-fx-control-inner-background: #2B2B2B; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6);"
  // Unified style for primary buttons in the UI (dark gray background like inputs)
  private val buttonStyle: String = "-fx-background-color: #2B2B2B; -fx-text-fill: white;"
  // self-register as observer
  gameController.add(this)
  Debug.log("WizardGUI constructed and registered as observer")

  // --- Undo/Redo toolbar helpers ---
  private def hideUndoRedoBar(): Unit = {
    (for { rp <- rootPane; bar <- undoRedoBar } yield (rp, bar)) foreach { case (rp, bar) =>
      Platform.runLater {
        try { rp.children.remove(bar) } catch { case _: Throwable => () }
      }
    }
    undoRedoBar = None
  }
  private def createUndoRedoBar(): HBox = {
    import wizard.undo.UndoService
    val undoBtn = new Button("↶") {
      tooltip = Tooltip("Undo")
      style = buttonStyle + "; -fx-font-size: 10px; -fx-padding: 2 5 2 5;"
    }
    val redoBtn = new Button("↷") {
      tooltip = Tooltip("Redo")
      style = buttonStyle + "; -fx-font-size: 10px; -fx-padding: 2 5 2 5;"
    }
    val saveBtn = new Button("Save Game") {
      style = buttonStyle + "; -fx-font-size: 10px; -fx-padding: 2 5 2 5;"
      onAction = _ => showSaveDialog()
    }
    undoBtn.onAction = _ => {
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            // If we are on the player-name screen, inform controller to reset selection
            if (currentScreen == "PlayerNames") {
              try { gameController.resetPlayerCountSelection() } catch { case _: Throwable => () }
            } else {
              // Otherwise, use the new controller undo which also notifies observers
              gameController.undo()
            }
          } catch {
            case _: Throwable => ()
          }
        }
      })
      t.setDaemon(true); t.start()
    }
    redoBtn.onAction = _ => {
      val t = new Thread(new Runnable { override def run(): Unit = try { gameController.redo() } catch { case _: Throwable => () } })
      t.setDaemon(true); t.start()
    }
    new HBox(6) { alignment = Pos.TopLeft; padding = Insets(8); children = Seq(undoBtn, redoBtn, saveBtn) }
  }
  private def ensureUndoRedoBarVisible(): Unit = {
    if (undoRedoBar.isEmpty) {
      val bar = createUndoRedoBar()
      try { bar.pickOnBounds = false } catch { case _: Throwable => () }
      undoRedoBar = Some(bar)
    }
    
    undoRedoBar.foreach { bar =>
      rootPane.foreach { rp =>
        Platform.runLater {
          val kids = rp.children
          if (!kids.contains(bar)) {
            // Remove from old parent if any
            try {
              val oldParent = bar.delegate.getParent
              if (oldParent != null && oldParent.isInstanceOf[javafx.scene.layout.Pane]) {
                oldParent.asInstanceOf[javafx.scene.layout.Pane].getChildren.remove(bar.delegate)
              }
            } catch { case _: Throwable => () }
            
            rp.children.add(bar)
            setAlignment(bar, Pos.TopLeft)
          }
          bar.toFront()
        }
      }
    }
  }

  // Perform immediate local navigation back to player-count UI (without waiting for controller event)
  private def goBackToPlayerCountLocal(): Unit = {
    navEpoch += 1 // cancel any pending transitions to names
    val thisEpoch = navEpoch
    Platform.runLater {
      Debug.log(s"WizardGUI.localBackToPlayerCount at epoch=$thisEpoch")
      currentScreen = "PlayerCount"
      contentBox match {
        case Some(box) =>
          box.children = buildPlayerCountChildren(box)
        case None =>
          if (stage != null && stage.scene() != null) {
            stage.scene().root = createInitialScreen()
          }
      }
    }
  }

  private def showSaveDialog(): Unit = {
    Platform.runLater {
      var saveBoxRef: VBox = null
      saveBoxRef = new VBox(10) {
        alignment = Pos.Center
        padding = Insets(20)
        style = "-fx-background-color: rgba(0,0,0,0.8); -fx-background-radius: 10;"
        maxWidth = 300
        maxHeight = 200
        children = Seq(
          new Label("Game title") { style = "-fx-text-fill: white; -fx-font-size: 16px;" },
          new TextField {
            id = "saveTitleField"
            promptText = "Enter title..."
            style = inputFieldStyle
            onAction = _ => {
              val title = this.text.value
              if (title.nonEmpty) {
                gameController.save(title)
                rootPane.foreach(_.children.remove(saveBoxRef))
              }
            }
          },
          new HBox(10) {
            alignment = Pos.Center
            children = Seq(
              new Button("Save") {
                style = buttonStyle
                onAction = _ => {
                  val field = saveBoxRef.lookup("#saveTitleField")
                  val title = (field: Any) match {
                    case tf: javafx.scene.control.TextField => tf.getText
                    case sn: scalafx.scene.Node if sn.delegate.isInstanceOf[javafx.scene.control.TextField] => 
                      sn.delegate.asInstanceOf[javafx.scene.control.TextField].getText
                    case _ => ""
                  }
                  if (title != null && title.nonEmpty) {
                    gameController.save(title)
                    rootPane.foreach(_.children.remove(saveBoxRef))
                  }
                }
              },
              new Button("Cancel") {
                style = buttonStyle
                onAction = _ => rootPane.foreach(_.children.remove(saveBoxRef))
              }
            )
          }
        )
      }
      rootPane.foreach(_.children.add(saveBoxRef))
    }
  }

  override def start(): Unit = {
    Debug.log("WizardGUI.start -> creating stage and initial screen")
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
    Debug.log("WizardGUI.start -> ensured observer registration and starting controller")
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
          Debug.log(s"WizardGUI.start -> applied pending PlayerCountSelected($cnt)")
          pendingPlayerCount = None
        }
      }
    }
  }

  private def buildPlayerCountChildren(ui: VBox): List[scalafx.scene.Node] = {
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
      alignment = Pos.Center
      style = inputFieldStyle
    }

    val nextButton = new Button("Weiter") {
      style = buttonStyle
    }

    // Controls responsiv binden (Breite) relativ zur UI-Box
    playerCountField.prefWidth <== ui.width * 0.3
    nextButton.prefWidth       <== ui.width * 0.3
    playerCountField.maxWidth  <== playerCountField.prefWidth
    nextButton.maxWidth        <== nextButton.prefWidth

    // Fonts dynamisch skalieren über CSS-Style relativ zur UI-Box
    titleLabel.style        <== Bindings.concat("-fx-text-fill: #39FF14; -fx-font-weight: bold; -fx-font-size: ", ui.width / 25, "px;")
    playerCountLabel.style  <== Bindings.concat("-fx-text-fill: black; -fx-font-size: ", ui.width / 35, "px;")
    playerCountField.style  <== Bindings.concat(inputFieldStyle, " -fx-font-size: ", ui.width / 40, "px;")
    nextButton.style        <== Bindings.concat(buttonStyle, " -fx-font-size: ", ui.width / 40, "px;")

    // Button-Logik
    nextButton.onAction = _ => {
      val playerCount = playerCountField.text.value
      if (playerCount.matches("[3-6]")) {
        val t = new Thread(new Runnable { override def run(): Unit = gameController.playerCountSelected(playerCount.toInt) })
        t.setDaemon(true)
        t.start()
        ui.children = createPlayerNameScreen(playerCount.toInt)
        currentScreen = "PlayerNames"
        ensureUndoRedoBarVisible()
      }
    }

    val resumeButton = new Button("Resume Game") {
      style = buttonStyle
      onAction = _ => showResumeDialog()
    }

    List(titleLabel, playerCountLabel, playerCountField, nextButton, resumeButton)
  }

  private def showResumeDialog(): Unit = {
    Platform.runLater {
      var resumeBoxRef: VBox = null
      resumeBoxRef = new VBox(10) {
        alignment = Pos.Center
        padding = Insets(20)
        style = "-fx-background-color: rgba(0,0,0,0.8); -fx-background-radius: 10;"
        maxWidth = 300
        maxHeight = 200
        children = Seq(
          new Label("Resume Game") { style = "-fx-text-fill: white; -fx-font-size: 16px;" },
          new Label("Enter save name:") { style = "-fx-text-fill: white;" },
          new TextField {
            id = "resumeTitleField"
            promptText = "Enter title..."
            style = inputFieldStyle
            onAction = _ => {
              val title = this.text.value
              if (title.nonEmpty) {
                gameController.load(title)
                rootPane.foreach(_.children.remove(resumeBoxRef))
              }
            }
          },
          new HBox(10) {
            alignment = Pos.Center
            children = Seq(
              new Button("Resume") {
                style = buttonStyle
                onAction = _ => {
                  val field = resumeBoxRef.lookup("#resumeTitleField")
                  val title = (field: Any) match {
                    case tf: javafx.scene.control.TextField => tf.getText
                    case sn: scalafx.scene.Node if sn.delegate.isInstanceOf[javafx.scene.control.TextField] => 
                      sn.delegate.asInstanceOf[javafx.scene.control.TextField].getText
                    case _ => ""
                  }
                  if (title != null && title.nonEmpty) {
                    gameController.load(title)
                    rootPane.foreach(_.children.remove(resumeBoxRef))
                  }
                }
              },
              new Button("Cancel") {
                style = buttonStyle
                onAction = _ => rootPane.foreach(_.children.remove(resumeBoxRef))
              }
            )
          }
        )
      }
      rootPane.foreach(_.children.add(resumeBoxRef))
    }
  }

  private def createInitialScreen(): StackPane = {
    // UI-Container (Vordergrund)
    val ui = new VBox(20) {
      alignment = Pos.Center
      padding = Insets(20)
    }
    // Keep a reference so we can switch content when TUI drives the flow
    contentBox = Some(ui)
    currentScreen = "PlayerCount"

    // Build initial children into the existing UI container
    ui.children = buildPlayerCountChildren(ui)

    // Hintergrundbild laden (aus Ressourcen) – nicht mehr hart fehlschlagen, wenn fehlt
    val bgRes = getClass.getResource("/images/Wizard_game_background2_GUI.png")
    val root = new StackPane
    rootPane = Some(root)
    if (bgRes != null) {
      val bgView = new ImageView(new Image(bgRes.toExternalForm)) { preserveRatio = false }
      root.children = Seq(bgView, ui)
      // Hintergrund an Root-Größe binden (exaktes Mitstrecken)
      bgView.fitWidth  <== root.width
      bgView.fitHeight <== root.height
    } else {
      // Fallback ohne Hintergrundbild
      root.children = Seq(ui)
    }

    // UI-Container responsiv an Root binden
    ui.prefWidth <== root.width * 0.8
    ui.maxWidth  <== root.width * 0.8
    ui.spacing   <== root.height * 0.03

    // Ensure undo/redo buttons are visible from the start
    ensureUndoRedoBarVisible()

    root
  }

  private def createPlayerNameRoot(playerCount: Int): StackPane = {
    val ui = new VBox(20) {
      alignment = Pos.Center
      padding = Insets(20)
    }

    // make this UI the current content for responsive bindings BEFORE creating children
    contentBox = Some(ui)
    currentScreen = "PlayerNames"
    ui.children = createPlayerNameScreen(playerCount)

    val bgRes = getClass.getResource("/images/Wizard_game_background2_GUI.png")
    val root = new StackPane
    rootPane = Some(root)
    if (bgRes != null) {
      val bgView = new ImageView(new Image(bgRes.toExternalForm)) { preserveRatio = false }
      root.children = Seq(bgView, ui)
      bgView.fitWidth <== root.width
      bgView.fitHeight <== root.height
    } else {
      root.children = Seq(ui)
    }

    // Ensure undo/redo buttons are visible from this screen on
    ensureUndoRedoBarVisible()

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

  // --- Game table helpers ---
  private def updateScores(): Unit = {
    (scoresTable, Option(currentPlayers)) match {
      case (Some(table), Some(players)) =>
        val rows = players.map(p => PlayerRow(p.name, p.roundBids.toString, p.points.toString))
        table.items = ObservableBuffer.from(rows)
        table.refresh() // Force rowFactory to re-evaluate styles
      case _ => ()
    }
  }

  private def updateCurrentBids(p: Player): Unit = {
    updateScores()
  }

  private def ensureGameTableRoot(): Unit = {
    if (gameRoot.isDefined) return

    val trump = new ImageView() { preserveRatio = true }
    val hand = new HBox(10) { alignment = Pos.Center; padding = Insets(10) }

    val centerLabel = new Label("") // placeholder for table area
    val trickBox = new HBox(10) { alignment = Pos.Center }
    trickBar = Some(trickBox)

    val table = new TableView[PlayerRow]() {
      columns ++= List(
        new TableColumn[PlayerRow, String] {
          text = "Name"
          cellValueFactory = { _.value.name }
          prefWidth = 100
        },
        new TableColumn[PlayerRow, String] {
          text = "Bid"
          cellValueFactory = { _.value.bid }
          prefWidth = 50
        },
        new TableColumn[PlayerRow, String] {
          text = "Pkt"
          cellValueFactory = { _.value.points }
          prefWidth = 50
        }
      )
      prefHeight = 150
      maxWidth = 210
      columnResizePolicy = TableView.ConstrainedResizePolicy
      selectionModel().cellSelectionEnabled = false
      selectionModel().selectionMode = javafx.scene.control.SelectionMode.SINGLE // Selection will be hidden via CSS anyway
      
      rowFactory = { _ =>
        val row = new javafx.scene.control.TableRow[PlayerRow]()
        row.itemProperty().addListener((_, _, newItem) => {
          if (newItem != null && activePlayerName.contains(newItem.name.value)) {
            if (!row.getStyleClass.contains("current-player-row")) {
              row.getStyleClass.add("current-player-row")
            }
          } else {
            row.getStyleClass.remove("current-player-row")
          }
        })
        // Also listen to activePlayerName changes if possible, but simpler to just refresh table items
        row
      }

      style = """
        -fx-background-color: transparent;
        -fx-control-inner-background: transparent;
        -fx-table-cell-border-color: transparent;
        -fx-table-header-border-color: transparent;
        -fx-padding: 0;
      """
    }
    // Zusätzliches CSS, um auch Header und leere Bereiche transparent zu machen
    val cssPath = getClass.getResource("/table_transparent.css")
    if (cssPath != null) {
      table.stylesheets.add(cssPath.toExternalForm)
    }
    scoresTable = Some(table)

    val tablePane = new BorderPane {
      top = new HBox { alignment = Pos.Center; padding = Insets(10); children = Seq(trump) }
      center = new StackPane { children = Seq(centerLabel, trickBox) }
      right = new VBox(20) { alignment = Pos.TopCenter; padding = Insets(-20, 10, 10, 10); children = Seq(table) }
      bottom = hand
      padding = Insets(10)
    }

    trumpView = Some(trump)
    handBar = Some(hand)

    currentScreen = "Game"

    // Background like initial screens
    val bgRes = getClass.getResource("/images/Wizard_game_background2_GUI.png")
    val root = new StackPane
    rootPane = Some(root)
    if (bgRes != null) {
      val bgView = new ImageView(new Image(bgRes.toExternalForm)) { preserveRatio = false }
      root.children = Seq(bgView, tablePane)
      bgView.fitWidth  <== root.width
      bgView.fitHeight <== root.height
    } else {
      root.children = Seq(tablePane)
    }

    // Ensure undo/redo bar is visible on the game table too
    ensureUndoRedoBarVisible()

    gameRoot = Some(tablePane)

    Platform.runLater {
      if (stage != null && stage.scene() != null) stage.scene().root = root
    }
  }

  private def setTrump(card: Card): Unit = {
    ensureGameTableRoot()
    val url = cardToImageUrl(card)
    trumpView.foreach { iv =>
      if (url != null) iv.image = new Image(url) else iv.image = null
      iv.fitHeight = 120
    }
  }

  private def renderHand(player: Player): Unit = {
    ensureGameTableRoot()
    val bar = handBar.get
    val images = player.hand.cards.zipWithIndex.map { case (c, idx) =>
      val iv = new ImageView()
      val url = cardToImageUrl(c)
      if (url != null) iv.image = new Image(url)
      iv.fitHeight = 140
      iv.preserveRatio = true
      iv.onMouseClicked = _ => {
        // Play the card immediately
        InputRouter.offer((idx + 1).toString)
        bar.children.clear()
        // The "Next Player" button will be shown via the "card played" event
      }
      iv
    }
    bar.children = images
  }

  private def renderTrick(): Unit = {
    ensureGameTableRoot()
    Debug.log(s"WizardGUI.renderTrick -> cards: ${currentTrickCards.size}")
    trickBar.foreach { bar =>
      val images = currentTrickCards.map { card =>
        val iv = new ImageView()
        val url = cardToImageUrl(card)
        if (url != null) iv.image = new Image(url)
        iv.fitHeight = 160
        iv.preserveRatio = true
        iv
      }
      bar.children = images
    }
  }

  private def showBidPrompt(player: Player): Unit = {
    ensureGameTableRoot()
    // Remove previous overlay if present
    for { hb <- handBar; ov <- bidOverlay } yield hb.children.remove(ov)

    // Build inline overlay at bottom-left
    val tf = new TextField() { promptText = s"${player.name}: Stichzahl"; style = inputFieldStyle }
    val ok = new Button("OK") { style = buttonStyle }
    val overlay = new HBox(10) { alignment = Pos.Center; children = Seq(tf, ok) }
    
    ok.onAction = _ => {
      val text = tf.text.value
      if (text != null && text.matches("\\d+")) {
        // Send the bid to the router immediately
        InputRouter.offer(text)
        // Clear everything in handBar immediately to hide the current player's state
        handBar.foreach(_.children.clear())
        bidOverlay = None
      }
    }
    // place left of the hand bar content
    handBar.foreach { hb => hb.children.insert(0, overlay) }
    bidOverlay = Some(overlay)
  }

  private def cardToImageUrl(card: Card): String = {
    // Expected filenames in resources/images/cards: e.g., Red_1.png ... Red_13.png, Wizard.png, Jester.png
    val base = "/images/cards/"
    val name = card.value match {
      case Value.WizardKarte => "Wizard.png"
      case Value.Chester => "Jester.png"
      case v => s"${card.color.toString}_${v.cardType()}.png"
    }
    val res = getClass.getResource(base + name)
    if (res != null) res.toExternalForm else null
  }

  // Guarded navigation: switch to player-name screen only if the captured epoch matches the current epoch
  private def switchToPlayerNames(count: Int, capturedEpoch: Int): Unit = {
    if (capturedEpoch != navEpoch) {
      Debug.log(s"WizardGUI.switchToPlayerNames skipped due to stale epoch (captured=$capturedEpoch, current=$navEpoch)")
      return
    }
    Debug.log(s"WizardGUI.switchToPlayerNames applying for count=$count at epoch=$capturedEpoch")
    contentBox match {
      case Some(box) =>
        box.children = createPlayerNameScreen(count)
        currentScreen = "PlayerNames"
        ensureUndoRedoBarVisible()
      case None =>
        if (stage != null && stage.scene() != null) {
          stage.scene().root = createPlayerNameRoot(count)
        }
    }
  }

  // Minimal observer implementation to keep GUI and TUI in sync via events
  override def update(updateMSG: String, obj: Any*): Any = {
    Debug.log(s"WizardGUI.update('$updateMSG') received on JavaFX?=${Platform.isFxApplicationThread}")
    updateMSG match {
      case "AskForPlayerCount" =>
        // Cancel any pending transitions by bumping the navigation epoch, then switch back to player-count UI
        navEpoch += 1
        val thisEpoch = navEpoch
        Platform.runLater {
          Debug.log(s"WizardGUI.update -> handling AskForPlayerCount at epoch=$thisEpoch")
          currentScreen = "PlayerCount"
          contentBox match {
            case Some(box) =>
              box.children = buildPlayerCountChildren(box)
            case None =>
              if (stage != null && stage.scene() != null) {
                stage.scene().root = createInitialScreen()
              }
          }
          ensureUndoRedoBarVisible()
        }
        ()
      case "AskForPlayerNames" =>
        Platform.runLater {
          Debug.log("WizardGUI.update -> handling AskForPlayerNames")
          // If we were on the game table, we need to clear it and reset scene root
          gameRoot = None 
          currentScreen = "PlayerNames"
          val count = selectedPlayerCount.getOrElse(3)
          // Always reset the root when switching back to names, to ensure visibility
          if (stage != null && stage.scene() != null) {
            stage.scene().root = createPlayerNameRoot(count)
          }
          ensureUndoRedoBarVisible()
        }
        ()
      case "PlayerCountSelected" =>
        val count = obj.headOption match {
          case Some(pcs: wizard.actionmanagement.PlayerCountSelected) => pcs.count
          case Some(i: Int) => i
          case _ => 0
        }
        Debug.log(s"WizardGUI.update -> PlayerCountSelected($count)")
        if (count >= 3 && count <= 6) {
          selectedPlayerCount = Some(count)
          // If stage/scene not ready yet, buffer the desired state change
          if (stage == null || stage.scene() == null) {
            Debug.log("WizardGUI.update -> stage not ready, buffering player count")
            pendingPlayerCount = Some(count)
          } else {
            val epoch = navEpoch
            Platform.runLater {
              Debug.log("WizardGUI.update -> attempting switch to player name screen")
              switchToPlayerNames(count, epoch)
              ensureUndoRedoBarVisible()
            }
          }
        } else {
          ()
        }
        ()
      case "CardsDealt" =>
        // Switch to game table; capture players for scoreboard and update UI.
        obj.headOption.collect { case cd: wizard.actionmanagement.CardsDealt => cd.players }.foreach { ps => currentPlayers = ps }
        Platform.runLater({
          ensureGameTableRoot()
          updateScores()
          renderTrick()
          ensureUndoRedoBarVisible()
        })
        ()
      case "card played" =>
        obj.headOption.collect { case c: Card => c }.foreach { card =>
          Debug.log(s"WizardGUI.update('card played') -> $card")
          Platform.runLater({
            if (!currentTrickCards.contains(card)) {
              currentTrickCards = currentTrickCards :+ card
            }
            renderTrick()
            
            // Clear handBar immediately
            handBar.foreach(_.children.clear())
          })
        }
        ()
      case "print trump card" =>
        obj.headOption.collect { case c: Card => c }.foreach { c => Platform.runLater(setTrump(c)) }
        ()
      case "ShowHand" =>
        val playerOpt: Option[Player] = obj.headOption match {
          case Some(sh: wizard.actionmanagement.ShowHand) => Some(sh.player)
          case Some(p: Player) => Some(p)
          case _ => None
        }
        playerOpt.foreach(p => Platform.runLater({
          activePlayerName = Some(p.name)
          updateCurrentBids(p)
          
          handBar.foreach { bar =>
            bar.children.clear()
            val nextBtn = new Button("Next Player: " + p.name) {
              style = buttonStyle
              onAction = _ => {
                renderHand(p)
              }
            }
            bar.children = Seq(nextBtn)
          }
        }))
        ()
      case "which card" =>
        obj.headOption.collect { case p: Player => p }.foreach { p => Platform.runLater({
          val targetText = "Next Player: " + p.name
          Debug.log(s"WizardGUI.update('which card') for ${p.name}. target: $targetText")
          activePlayerName = Some(p.name)
          updateCurrentBids(p)
          
          handBar.foreach { bar =>
            val currentlyShowingNext = bar.children.exists(node => node.isInstanceOf[javafx.scene.control.Button] && node.asInstanceOf[javafx.scene.control.Button].getText.startsWith("Next Player"))
            val showingTarget = bar.children.exists(node => node.isInstanceOf[javafx.scene.control.Button] && node.asInstanceOf[javafx.scene.control.Button].getText == targetText)
            
            if (!showingTarget) {
                Debug.log(s"WizardGUI -> showing Next Player button for ${p.name}")
                bar.children.clear()
                val nextBtn = new Button(targetText) {
                  style = buttonStyle
                  onAction = _ => {
                    renderHand(p)
                  }
                }
                bar.children = Seq(nextBtn)
            } else {
                Debug.log(s"WizardGUI -> already showing button for ${p.name}")
            }
          }
        }) }
        ()
      case "which bid" =>
        obj.headOption.collect { case p: Player => p }.foreach { p =>
          Platform.runLater({
            activePlayerName = Some(p.name)
            updateCurrentBids(p)
            
            handBar.foreach { bar =>
              bar.children.clear()
              val nextBtn = new Button("Next Player: " + p.name) {
                style = buttonStyle
                onAction = _ => {
                  renderHand(p)
                  showBidPrompt(p)
                }
              }
              bar.children = Seq(nextBtn)
            }
          })
        }
        ()
      case "TrickUpdated" =>
        obj.headOption.collect { case cards: List[Card] => cards }.foreach { cards =>
          Debug.log(s"WizardGUI.update('TrickUpdated') -> ${cards.size} cards: ${cards.mkString(", ")}")
          Platform.runLater({
            currentTrickCards = cards
            renderTrick()
          })
        }
        ()
      case "LoadFailed" =>
        val title = obj.headOption.collect { case s: String => s }.getOrElse("Unknown")
        Platform.runLater {
          val alertBox = new VBox(10) {
            alignment = Pos.Center
            padding = Insets(20)
            style = "-fx-background-color: rgba(200,0,0,0.9); -fx-background-radius: 10;"
            maxWidth = 250
            children = Seq(
              new Label(s"'$title' not found") { style = "-fx-text-fill: white; -fx-font-weight: bold;" },
              new Button("OK") {
                style = buttonStyle
                onAction = _ => rootPane.foreach(_.children.remove(this.parent.value))
              }
            )
          }
          rootPane.foreach(_.children.add(alertBox))
        }
        ()
      case "GameLoaded" =>
        obj.headOption.collect { case g: wizard.model.Game => g }.foreach { game =>
          Platform.runLater({
            currentPlayers = game.players
            currentTrickCards = game.currentTrick
            ensureGameTableRoot()
            updateScores()
            renderTrick()
            ensureUndoRedoBarVisible()
          })
        }
        ()
      case "UndoPerformed" | "RedoPerformed" =>
        Platform.runLater({
          updateScores()
          // Refresh hand if we are in game
          if (currentScreen == "Game") {
             // Synchronize trick cards in GUI with the one in controller loop if possible
             // Since trick is local to the loop, we might need to send it via event
             // For now, if undo happens, the loop should re-emit the trick state or we clear it
          }
        })
        ()
      case _ => ()
    }
  }

  // ---- Test helpers (package-private for unit tests) ----
  private[aView] def testBuildPlayerNameRoot(count: Int): StackPane = createPlayerNameRoot(count)
  private[aView] def testUndoRedoBarPresent: Boolean = undoRedoBar.isDefined
  private[aView] def testContentBoxRef: AnyRef = contentBox.orNull
  private[aView] def testCurrentScreen: String = currentScreen
  private[aView] def testGetNavEpoch: Int = navEpoch
  private[aView] def testSwitchToPlayerNames(count: Int, capturedEpoch: Int): Unit = switchToPlayerNames(count, capturedEpoch)
  private[aView] def testSimulateUndoFromNames(): Unit = {
    if (currentScreen == "PlayerNames") {
      currentScreen = "PlayerCount"
      contentBox.foreach { box =>
        box.children = buildPlayerCountChildren(box)
      }
    }
  }
  private[aView] def testLocalBackToPlayerCount(): Unit = goBackToPlayerCountLocal()
}

object WizardGUI {
  // Fallback launcher creating its own controller if started standalone
  def main(args: Array[String]): Unit = {
    val controller = new GameLogic
    val app = new WizardGUI(controller)
    app.main(args)
  }
}