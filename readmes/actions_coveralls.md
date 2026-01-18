# GitHub Actions & Coveralls

### Continuous Integration (GitHub Actions)
Bei jedem Push oder Pull-Request auf den `master`-Branch wird automatisch eine CI-Pipeline gestartet. Diese baut das Projekt und führt alle Tests in einer sauberen Umgebung aus. Die Konfiguration findet sich in `.github/workflows/scala.yml`.

### Code Coverage mit Coveralls
Zusätzlich zur Testausführung wird die Testabdeckung mit `sbt-scoverage` gemessen und an **Coveralls** "übermittelt".

### Badges
Der aktuelle Status des Builds und der Code-Coverage ist direkt in der [ROOT README](../README.md) in der ersten Zeile zu finden.
