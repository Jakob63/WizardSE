# Design Patterns

Um die Qualität und Erweiterbarkeit des Codes zu gewährleisten, wurden verschiedene etablierte Entwurfsmuster eingesetzt.

### 1. Observer Pattern
Dies ist das Rückgrat unserer UI-Synchronisation.
- **Subject:** Der Controller (`GameLogic`) benachrichtigt registrierte Observer über Spielereignisse.
- **Observer:** `TextUI` und `WizardGUI` reagieren auf diese Benachrichtigungen, um die Anzeige zu aktualisieren.

### 2. Factory Pattern
Wird zur Erzeugung von Spielern genutzt, um die Erstellungslogik zu kapseln.
- **`PlayerFactory`:** Ermöglicht die einfache Erstellung von `Human` oder `AI` Spielern, ohne dass der aufrufende Code die konkreten Klassen kennen muss.

### 3. Command Pattern
Wie in der [Undo/Redo Dokumentation](undo.md) beschrieben, nutzen wir Kommandos, um Aktionen speicher- und widerrufbar zu machen.

### 4. Singleton / Module Pattern
Über Google Guice realisieren wir das Singleton-Verhalten für wichtige Komponenten wie das FileIO-System, was die Testbarkeit und Austauschbarkeit erhöht.
