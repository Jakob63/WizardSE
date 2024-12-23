ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "ProjektSE"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"

Test / testOptions += Tests.Filter(_.equals("wizard.aTestSequence.TestSequence"))
