# Graphical User Interface (GUI)

Die grafische Benutzeroberfläche des Spiels wurde mit **ScalaFX** (einem Scala-Wrapper für JavaFX) umgesetzt.

### Features der GUI
- Intuitive Darstellung der Spielkarten und des Spieltisches.
- Interaktive Eingabe von Spielernamen, Geboten und Kartenauswahl.
- Visuelles Feedback bei Spielereignissen.

### Zusammenspiel mit der TUI (Dual-UI)
Eine Besonderheit des Projekts ist, dass GUI und TUI simultan betrieben werden können. Dies wird durch folgende Architektur ermöglicht:
- **Observer-Pattern:** Sowohl `WizardGUI` als auch `TextUI` sind Observer des Controllers. Jede Änderung im Spielzustand triggert ein Update in beiden Oberflächen.
- **Event-basierte TUI:** Die TUI ist so konzipiert, dass sie auf Controller-Events reagiert und Eingaben an den Controller weiterleitet, genau wie die GUI.
- **Synchronität:** Aktionen in der GUI (z.B. Karte spielen) werden sofort in der TUI reflektiert und umgekehrt.


