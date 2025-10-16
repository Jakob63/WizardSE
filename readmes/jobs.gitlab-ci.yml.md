# jobs.gitlab-ci.yml

- **Scala/SBT:** Verwendet sbt-Image, sbt clean compile und sbt test wie in den Guidelines beschrieben
- **Tests:** ScalaTest wird unterstützt; Headless-Flags verhindern GUI-Probleme in CI. Die Views bleiben dünn; Controller/Model werden deterministisch getestet
- **Schneller:** Coursier/Ivy/SBT Cache macht Builds stabil und schneller
- **Artefakte:** Kompilierte Klassen und JARs können aus der Pipeline heruntergeladen werden. JUnit-Reports sind optional, falls konfiguriert