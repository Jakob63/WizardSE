# Text User Interface (TUI)

Die TUI war der erste Schritt in der Entwicklung des User Interfaces und dient heute als vollwertige Alternative zur GUI.

### Funktionalität
- **Kommando-basiert:** Der Spieler interagiert über Textkommandos mit dem Spiel (z.B. Eingabe von Zahlen für Gebote oder Kartenauswahl).
- **Zustandsanzeige:** Nach jedem Zug wird der aktuelle Spielzustand (Handkarten, Tisch, Punktestand) in der Konsole ausgegeben.
- **MVC Integration:** Die `TextUI` implementiert das `Observer`-Interface und wird vom Controller bei jeder Statusänderung aktualisiert.

### Dual-UI Betrieb
Wie in der [GUI Dokumentation](GUI.md) beschrieben, arbeiten TUI und GUI parallel. Das bedeutet:
- Man kann eine Karte in der GUI anklicken und sieht die Reaktion sofort in der TUI.
- Man kann einen Befehl in die Konsole tippen und die GUI aktualisiert sich entsprechend.
