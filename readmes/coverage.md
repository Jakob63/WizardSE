# Testing & Coverage

Qualitätssicherung durch automatisierte Tests ist ein integraler Bestandteil dieses Projekts.

### ScalaTest & BDD
Wir verwenden das **ScalaTest** Framework, um das Verhalten des Systems zu verifizieren. Dabei setzen wir auf den **Behaviour Driven Development (BDD)** Stil mit `AnyWordSpec`. Dies erlaubt es uns, Tests in einer menschenlesbaren Form zu schreiben.

Beispiel aus `RoundLogicTest.scala`:
```scala
"RoundLogic" should {
  "correctly determine the winner of a trick" in {
    // Testlogik...
  }
}
```

### Testabdeckung
Unser Ziel ist eine hohe Code-Coverage, insbesondere in den kritischen Bereichen des Domain-Models und der Controller-Logik. 
- **Tool:** `sbt-scoverage` misst die Abdeckung während der Testläufe.
- **Visualisierung:** Die Ergebnisse werden an Coveralls übertragen und sind über das Badge in der README einsehbar.

Die Tests können einfach über die IDE (ScalaTest Runner) oder per Terminal mit `sbt test` gestartet werden.
