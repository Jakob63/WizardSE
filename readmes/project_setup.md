# Project Setup

Die Basis des Projekts bildet eine solide Konfiguration mit **sbt** (Scala Build Tool).

### sbt Konfiguration
- **`build.sbt`:** Hier sind alle Abhängigkeiten (wie ScalaFX, Guice, Play-JSON) und Projekt-Metadaten definiert.
- **Plugins:** Wir nutzen Plugins für Code-Coverage (`sbt-scoverage`) und zur statischen Code-Analyse.

### Entwicklungs-Workflow
1. **Initialisierung:** Das Projekt startete mit einem einfachen Skelett und einer ersten TUI-Ausgabe zur Verifizierung der Logik.
2. **Modularisierung:** Die Struktur wurde frühzeitig in Model, View und Controller Pakete unterteilt.
3. **IDE-Support:** Dank der standardisierten sbt-Struktur lässt sich das Projekt problemlos in IntelliJ IDEA oder VS Code importieren und starten.