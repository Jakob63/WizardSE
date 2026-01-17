# MVC Architektur

Das Projekt folgt strikt dem **Model-View-Controller (MVC)** Muster, um eine klare Trennung zwischen Daten, Logik und Präsentation zu gewährleisten.

### Model (`wizard.model.*`)
Das Model verwaltet den Zustand des Spiels und die Business-Logik.
- **Entities:** `Player`, `Card`, `Hand`, `Dealer`.
- **Zustand:** `Game`, `Round`.
- Das Model ist unabhängig von der Benutzeroberfläche und enthält keine UI-Logik.

### Controller (`wizard.controller.*`)
Der Controller fungiert als Bindeglied zwischen Model und View.
- **Logik:** `GameLogic` und `RoundLogic` steuern den Spielablauf.
- **Observable:** Der Controller hält eine Liste von Observern (Views) und benachrichtigt diese über Statusänderungen.

### View (`wizard.aView.*`)
Die Views sind für die Darstellung und die Entgegennahme von Benutzereingaben zuständig.
- **TUI:** `TextUI` für die Konsole.
- **GUI:** `WizardGUI` für die grafische Oberfläche.
- Views kennen das Model (lesend) und rufen Methoden am Controller auf, halten aber keine eigene Spiellogik.
