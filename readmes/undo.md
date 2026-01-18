# Undo / Redo (Command Pattern)

Um Fehlentscheidungen korrigieren zu können, wurde ein Undo/Redo-Mechanismus implementiert.

### Command Pattern
Jede wesentliche Spielaktion (z.B. Spielstart, Karte spielen) wird als Kommando-Objekt gekapselt.
- **`UndoManager`:** Verwaltet zwei Stacks für rückgängig gemachte und ausgeführte Kommandos.
- **`StartGameCommand`:** Ermöglicht das Zurücksetzen des gesamten Spielstarts.
- **`PlayCardCommand`:** Kapselt das Ausspielen einer Karte (aktuell in Vorbereitung für die Integration in den Spielfluss).

### Funktionaler Stil
Bei der Implementierung wurde strikt auf moderne Scala-Prinzipien geachtet:
- **Kein `null`:** Stattdessen wird konsequent `Option` verwendet.
- **Fehlerbehandlung:** Anstelle von `try-catch` Blöcken nutzen wir die `Try`-Monade (`Success`/`Failure`), um Seiteneffekte und Fehler sicher zu handhaben.


