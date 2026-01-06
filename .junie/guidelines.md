Project Development Guidelines

Last updated: 2025-10-09

Purpose
These guidelines capture conventions and constraints specific to this repository to help future contributors work efficiently and consistently.

Scope and Non‑negotiable Constraints
- Programming language: Scala (keep Scala throughout; do not introduce other primary languages).
- Architectural pattern: MVC must be preserved.
- Design patterns: Keep the existing patterns, in particular the Observer pattern used between controller and views.
- Dual UI: Both TUI and GUI must remain usable interchangeably; they observe the same controller so that actions in one are reflected in the other.
- Dual GUI: Both TUI and GUI must be launched. No optional launch for either of them.

Current High‑Level Architecture (MVC + Observer)
- Model (wizard.model.*)
  - Domain entities and game state. Examples: wizard.model.player.Human, wizard.model.player.AI.
- Controller (wizard.controller.*)
  - Game coordination and state transitions. Examples: GameLogic, PlayerLogic.
  - Acts as the Observable/Subject in the Observer pattern. Views register as observers and are notified on state changes.
- Views (wizard.aView.*)
  - TUI: wizard.aView.TextUI
  - GUI: wizard.aView.aView_GUI.WizardGUI (ScalaFX/JavaFX based)
  - Both views subscribe to the same controller instance. The GUI registers itself as an observer in its constructor. The TUI should also register or otherwise react to controller updates. This guarantees that TUI and GUI remain synchronized, allowing users to mix their usage freely during a session.

Observer Pattern Expectations
- The controller exposes registration for observers (directly or via a helper). Views must subscribe on construction or initialization.
- On any state mutation in the controller (e.g., new round, card played, bid updated), the controller must notify all observers.
- Observer callbacks in views must perform minimal logic: update display state and request re-render; avoid heavy business logic in views.
- Do not introduce view-to-view dependencies; communication flows through the controller only.

Testing Information
- OVERRIDE: do note write test, do not adjust exisiting tests. Ignore the tests. Ignore the other Stichpunkte hier unter "Testing Information" for now. i will remove this line, when i am ready.
- Always add tests for any new content you generate. No feature or bugfix should be merged without tests.
- Test framework: ScalaTest (already used in this repo). Keep new tests consistent with existing style.
- Locations:
  - Unit and integration tests: src/test/scala/... mirroring the main source package structure.
- Naming conventions:
  - For suites: <ClassName>Test.scala or <ClassName>Tests.scala (both patterns exist; prefer consistency with nearby code).
  - For behavior-style tests, use descriptive "it should ..." clauses as exemplified in existing suites.
- Running tests (Windows/PowerShell):
  - sbt test
  - To run a single suite: sbt "testOnly wizard.controller.PlayerLogicTests"
  - To run one test by name: sbt "testOnly wizard.controller.PlayerLogicTests -- -z \"bid correctly\""
- Code coverage (optional but recommended): If you add sbt-scoverage, aim for meaningful coverage of controller and model logic. Do not block builds unless team agrees.
- What to test for this project:
  - Model: Player behaviors (e.g., AI bidding and card selection), validation rules.
  - Controller: Scoring, bidding, trick flow, and state transition side-effects (notifications to observers can be verified indirectly via state changes or with test observers).
  - Views: Keep logic thin; restrict tests to presentation helpers or use test doubles for observer wiring if necessary.
- Fast feedback: Prefer small, deterministic tests without randomness. When randomness is required (e.g., shuffling), inject a seeded RNG.

Development Guidelines
- Preserve MVC boundaries:
  - Model: Pure domain/state; no UI or I/O.
  - Controller: Orchestrates model; exposes commands/events; no direct UI code, only observer notifications and simple adapters.
  - Views (GUI/TUI): Rendering and user input only; forward commands to controller.
- Keep Observer intact:
  - Do not remove observer registration from WizardGUI or TextUI.
  - Any new view must register and respond to controller updates.
- Dependency direction:
  - Views may depend on controller interfaces; controller should not depend on concrete views.
  - Prefer traits for observer interfaces to simplify testing and reduce coupling.
- Threading and UI updates:
  - GUI (ScalaFX/JavaFX) updates must occur on the JavaFX Application Thread. Use Platform.runLater for cross-thread notifications if controller updates might happen off the UI thread.
- Error handling and logging:
  - Controllers should validate inputs and provide clear error signals (Either/Option/Exceptions) that views can present.
  - Avoid logging noise. The main launcher suppresses JavaFX logging; keep it that way to reduce console clutter.

How TUI and GUI Interoperate
- Single source of truth: One controller instance is shared by both UIs (see wizard.Wizard.main where TextUI and WizardGUI receive the same GameLogic instance).
- Registration: GUI registers as observer in its constructor; TUI should either observe or poll state consistently.
- User flow: Actions from TUI or GUI must invoke controller methods; updates propagate to all observers so both UIs stay in sync.

Coding Conventions
- Scala version: Use the project’s configured Scala version from build.sbt.
- Functional style preferred in the model and controller (immutability where feasible), but pragmatic where necessary for UI interop.
- Use meaningful names; keep methods short and focused.
- Keep public APIs small; prefer package-private/ private[wizard] where appropriate.

Adding New Features
- Start with controller contracts (methods/commands), then model changes, then views.
- Add or update tests first (TDD encouraged):
  1) Write/extend tests in src/test/scala mirroring the packages you touch.
  2) sbt test and iterate until green.
- Wire observer notifications for every state change that affects rendering.
- Confirm both TUI and GUI reflect changes before concluding the feature.

Refactoring Rules
- Preserve existing package structure and naming unless a compelling reason exists.
- Do not delete MVC layers, observer hooks, or patterns already present.
- If you must introduce new patterns (e.g., Strategy for AI), do so without breaking existing observer contracts.

Performance and Resources
- Avoid blocking UI threads. Long operations should be offloaded and updates marshaled back to the UI thread.
- Keep allocations modest in render loops; cache where sensible in views.

Release/Verification Checklist
- All tests pass locally: sbt test (Verified on 2025-10-09: existing test suites AI_Test and PlayerLogicTests pass 100%).
- Manual sanity check:
  - Start application (Wizard.main). Confirm GUI launches and reacts to controller events.
  - Optionally run TUI commands; verify both UIs stay in sync.

Housekeeping
- Do not add non-essential files to the repository root.
- If you create any helper or temporary files/scripts during development, delete them before committing. The only file added by this task is .junie/guidelines.md.

Contact/Notes
- Keep this document updated when adding significant modules, patterns, or build tooling.
