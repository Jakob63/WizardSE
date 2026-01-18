# GitHub Actions & Coveralls

Dieses Projekt nutzt moderne CI/CD-Tools, um die Qualität des Codes sicherzustellen.

### sbt als Build-System
Die Entwicklung ist plattformunabhängig durch den Einsatz von **sbt**. Alle Abhängigkeiten, Kompilierungsschritte und Tests werden darüber gesteuert.

### Continuous Integration (GitHub Actions)
Bei jedem Push oder Pull-Request auf den `master`-Branch wird automatisch eine CI-Pipeline gestartet. Diese baut das Projekt und führt alle Tests in einer sauberen Umgebung aus. Die Konfiguration findet sich in `.github/workflows/scala.yml`.

### Code Coverage mit Coveralls
Zusätzlich zur Testausführung wird die Testabdeckung gemessen (mittels `sbt-scoverage`) und an **Coveralls** übermittelt. So ist jederzeit ersichtlich, welche Teile des Codes durch Tests abgedeckt sind.

### Badges
Der aktuelle Status des Builds und der Code-Coverage ist direkt in der [ROOT README](../README.md) über Badges ersichtlich.
