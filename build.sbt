ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "ProjektSE",
    Compile / run / mainClass := Some("wizard.Wizard")
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"
libraryDependencies += "org.scalafx" %% "scalafx" % "22.0.0-R33"

// Ensure OpenJFX native libraries are present for the current OS. Needed by ScalaFX.
// The classifier must match the platform: "win", "linux", or "mac".
// SBT evaluates System.getProperty at build time, which is fine for local development.
Compile / libraryDependencies ++= {
  val os = System.getProperty("os.name").toLowerCase
  val platform =
    if (os.contains("win")) "win"
    else if (os.contains("mac")) "mac"
    else "linux"
  Seq(
    "org.openjfx" % "javafx-base" % "22.0.2" classifier platform,
    "org.openjfx" % "javafx-graphics" % "22.0.2" classifier platform,
    "org.openjfx" % "javafx-controls" % "22.0.2" classifier platform
  )
}

Test / testOptions += Tests.Filter(_.equals("wizard.aTestSequence.TestSequence"))
