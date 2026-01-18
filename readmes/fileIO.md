# File IO (JSON / XML)

Das Projekt bietet die Möglichkeit, Spielstände sowohl im **JSON**- als auch im **XML**-Format zu speichern und zu laden.

### Einheitliches Interface
Beide Implementierungen basieren auf dem `FileIOInterface`. Dies stellt sicher, dass der Controller (`GameLogic`) agnostisch gegenüber dem gewählten Speicherformat ist.

### Implementierungen
- **XML:** Nutzt die Standard Scala XML-Bibliothek, um den Spielzustand (Spieler, Punkte, Karten, Trumpf) in einer strukturierten `.xml` Datei abzulegen.
- **JSON:** Verwendet die `play-json` Bibliothek, um den Zustand in einer `.json` Datei zu serialisieren.

### Wechsel via Dependency Injection
Der Wechsel zwischen den Formaten erfolgt ohne Code-Änderung in der Spiellogik. In der Datei `src/main/scala/wizard/WizardModule.scala` kann die gewünschte Implementierung einfach einkommentiert werden. Der Controller erkennt beim Laden zudem automatisch die Dateiendung.


