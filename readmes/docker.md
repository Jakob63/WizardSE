# Docker

Die Dockerisierung des Projekts ist aktuell noch in der Planungsphase und wurde noch nicht final umgesetzt.

Zukünftig soll ein `Dockerfile` erstellt werden, das:
1. Eine passende JRE/JDK bereitstellt.
2. Den sbt-Build ausführt.
3. Das Spiel (vorrangig die TUI) in einem Container ausführbar macht.

Aktuell wird das Projekt nativ über sbt oder die IDE gestartet.