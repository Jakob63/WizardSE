package wizard.aView.aView_GUI

import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{VBox, StackPane, HBox, BorderPane}
import scalafx.scene.layout.StackPane.setAlignment
import scalafx.scene.text.{Font, FontWeight}
import scalafx.Includes._
import scalafx.application.Platform
import javafx.beans.binding.Bindings
import wizard.controller.aGameLogic
import wizard.actionmanagement.{Observer, Debug, InputRouter}
import wizard.model.player.{PlayerFactory, PlayerType, Player}
import wizard.model.cards.{Card, Color, Value}
import scalafx.scene.Node
import java.util.NoSuchElementException

class WizardGUI(val gameController: aGameLogic) extends JFXApp3 with Observer {
  // self-register as observer
  gameController.add(this)
  
  // keep reference to current root for overlay management
  private var rootPane: Option[StackPane] = None
  // top-left undo/redo toolbar (visible from player-name stage onwards)
  private var undoRedoBar: Option[HBox] = None
  // Buffer to handle events arriving before the JavaFX Stage is ready
  private var pendingPlayerCount: Option[Int] = None
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
  // UI elements for per-player info
  private var scoresBox: Option[VBox] = None
  // Unified style for all text input fields (dark gray like the player count box)
  private val inputFieldStyle: String = "-fx-control-inner-background: #2B2B2B; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6);"
  // Unified style for primary buttons in the UI (dark gray background like inputs)
  private val buttonStyle: String = "-fx-background-color: #2B2B2B; -fx-text-fill: white;"
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
    val undoBtn = new Button("↶") { tooltip = null; style = buttonStyle }
    val redoBtn = new Button("↷") { tooltip = null; style = buttonStyle }
    undoBtn.onAction = _ => {
      // Immediate local navigation when on PlayerNames for responsive UX
      if (currentScreen == "PlayerNames") {
        Debug.log("WizardGUI.undo -> local back to player count from PlayerNames")
        goBackToPlayerCountLocal()
      }
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          try {
            // If we are on the player-name screen, also inform controller to reset selection for cross-view sync
            if (currentScreen == "PlayerNames") {
              try { gameController.resetPlayerCountSelection() } catch { case _: Throwable => () }
            } else {
              // In other screens, delegate to global undo manager only
              try { UndoService.manager.undoStep() } catch { case _: Throwable => () }
            }
          } catch {
            case _: Throwable => ()
          }
        }
      })
      t.setDaemon(true); t.start()
    }
    redoBtn.onAction = _ => {
      val t = new Thread(new Runnable { override def run(): Unit = try { UndoService.manager.redoStep() } catch { case _: Throwable => () } })
      t.setDaemon(true); t.start()
    }
    new HBox(6) { alignment = Pos.TopLeft; padding = Insets(8); children = Seq(undoBtn, redoBtn) }
  }
  private def ensureUndoRedoBarVisible(): Unit = {
    if (undoRedoBar.isEmpty) {
      val bar = createUndoRedoBar()
      // Prevent the bar from intercepting mouse clicks outside its visible buttons
      try { bar.pickOnBounds = false } catch { case _: Throwable => () }
      undoRedoBar = Some(bar)
    }
    
    undoRedoBar.foreach { bar =>
      rootPane.foreach { rp =>
        Platform.runLater {
          val kids = rp.children
          if (!kids.contains(bar)) {
            kids.add(bar)
          }
          bar.toFront()
          StackPane.setAlignment(bar, Pos.TopLeft)
          Debug.log(s"WizardGUI.ensureUndoRedoBarVisible -> bar added to rootPane and aligned TopLeft")
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
      hideUndoRedoBar()
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
    // Observer registration should have happened in constructor or by external caller.
    // If we were re-started, ensure we are registered.
    gameController.add(this)
    Debug.log("WizardGUI.start -> stage created and observer presence ensured")
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
        val t = new Thread(new Runnable { override def run(): Unit = {
          gameController.playerCountSelected(playerCount.toInt) 
        }})
        t.setDaemon(true)
        t.start()
        ui.children = createPlayerNameScreen(playerCount.toInt)
        currentScreen = "PlayerNames"
        ensureUndoRedoBarVisible()
      }
    }

    List(titleLabel, playerCountLabel, playerCountField, nextButton)
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
        val t = new Thread(new Runnable { override def run(): Unit = gameController.setPlayers(players) })
        t.setDaemon(true)
        t.start()
      }
    }

    List(titleLabel) ++ playerFields :+ startButton
  }

  // --- Game table helpers ---
  private def updateScores(): Unit = {
    scoresBox.foreach { box =>
      val header = new HBox(10) {
        children = Seq(
          new Label("Spieler") { prefWidth = 80; style = "-fx-text-fill: #39FF14; -fx-font-weight: bold;" },
          new Label("Bids") { prefWidth = 40; style = "-fx-text-fill: #39FF14; -fx-font-weight: bold;" },
          new Label("Points") { prefWidth = 50; style = "-fx-text-fill: #39FF14; -fx-font-weight: bold;" }
        )
      }
      val rows = currentPlayers.map { p =>
        new HBox(10) {
          children = Seq(
            new Label(p.name) { prefWidth = 80; style = "-fx-text-fill: white;" },
            new Label(p.roundBids.toString) { prefWidth = 40; style = "-fx-text-fill: white;" },
            new Label(p.points.toString) { prefWidth = 50; style = "-fx-text-fill: white;" }
          )
        }
      }
      box.children = header :: rows
    }
  }

  private def ensureGameTableRoot(): Unit = {
    if (gameRoot.isDefined) return

    val trump = new ImageView() { preserveRatio = true }
    val hand = new HBox(10) { alignment = Pos.Center; padding = Insets(10) }

    val trumpLabel = new Label("Trump:") {
      style = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;"
    }
    val trumpContainer = new VBox(5) {
      alignment = Pos.Center
      children = Seq(trumpLabel, trump)
    }

    val centerLabel = new Label("") // placeholder for table area

    val scores = new VBox(6) {
      alignment = Pos.TopRight
      padding = Insets(0)
      pickOnBounds = false
    }
    scoresBox = Some(scores)

    val tablePane = new BorderPane {
      top = new HBox { alignment = Pos.Center; padding = Insets(10); children = Seq(trumpContainer) }
      center = new StackPane { children = Seq(centerLabel) }
      bottom = hand
      padding = Insets(5)
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
      root.children = Seq(bgView, tablePane, scores)
      bgView.fitWidth  <== root.width
      bgView.fitHeight <== root.height
    } else {
      root.children = Seq(tablePane, scores)
    }

    Platform.runLater {
      StackPane.setAlignment(scores, Pos.TopRight)
      scores.toFront()
    }

    // Ensure undo/redo bar is visible on the game table too
    ensureUndoRedoBarVisible()

    gameRoot = Some(tablePane)

    Platform.runLater {
      if (stage != null && stage.scene() != null) {
        stage.scene().root = root
      }
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
        // Provide 1-based index to the model as expected by Human.playCard
        InputRouter.offer((idx + 1).toString)
      }
      iv
    }
    bar.children = images
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
        InputRouter.offer(text)
        // Keep overlay visible, but disable input and change to Next Player (disabled) until next prompt arrives
        tf.disable = true
        ok.text = "Next Player"
        ok.disable = true
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
    updateMSG match {
      case "input players" | "AskForPlayerCount" =>
        // Cancel any pending transitions by bumping the navigation epoch, then switch back to player-count UI
        navEpoch += 1
        val thisEpoch = navEpoch
        Platform.runLater {
          Debug.log(s"WizardGUI.update -> handling AskForPlayerCount at epoch=$thisEpoch")
          currentScreen = "PlayerCount"
          hideUndoRedoBar()
          contentBox match {
            case Some(box) =>
              box.children = buildPlayerCountChildren(box)
            case None =>
              if (stage != null && stage.scene() != null) {
                stage.scene().root = createInitialScreen()
              }
          }
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
          // If stage/scene not ready yet, buffer the desired state change
          if (stage == null || stage.scene() == null) {
            Debug.log("WizardGUI.update -> stage not ready, buffering player count")
            pendingPlayerCount = Some(count)
          } else {
            val epoch = navEpoch
            Platform.runLater {
              Debug.log("WizardGUI.update -> attempting switch to player name screen")
              switchToPlayerNames(count, epoch)
            }
          }
        } else {
          ()
        }
        ()
      case "CardsDealt" =>
        // Switch to game table; capture players for scoreboard and update UI.
        obj.headOption.collect {
          case cd: wizard.actionmanagement.CardsDealt => cd.players
          case players: List[Player] => players
        }.foreach { ps => currentPlayers = ps }
        Platform.runLater({
          ensureGameTableRoot()
          updateScores()
        })
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
          renderHand(p)
        }))
        ()
      case "which card" =>
        obj.headOption.collect { case p: Player => p }.foreach { p => Platform.runLater({
          renderHand(p)
        }) }
        ()
      case "which bid" =>
        obj.headOption.collect { case p: Player => p }.foreach { p =>
          Platform.runLater({
            renderHand(p)
            showBidPrompt(p)
          })
        }
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
      hideUndoRedoBar()
      contentBox.foreach { box =>
        box.children = buildPlayerCountChildren(box)
      }
    }
  }
  private[aView] def testLocalBackToPlayerCount(): Unit = goBackToPlayerCountLocal()
}