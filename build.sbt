ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "ProjektSE",
    Compile / run / mainClass := Some("wizard.Wizard"),
    assembly / mainClass := Some("wizard.Wizard")
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"
libraryDependencies += "org.scalafx" %% "scalafx" % "22.0.0-R33"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.6"
libraryDependencies += "com.google.inject" % "guice" % "7.0.0"
libraryDependencies += "net.codingwell" %% "scala-guice" % "7.0.0"

Test / parallelExecution := false

Test / testOptions += Tests.Setup(() => println("Setting up tests..."))

Test / testOptions += Tests.Filter { name =>
  val excluded = Set[String]()
  !excluded.contains(name)
}

def testOrder(name: String): Int = name match {
  case n if n.startsWith("wizard.model.cards.Card") => 1
  case n if n.startsWith("wizard.model.cards.Hand") => 2
  case n if n.startsWith("wizard.model.Game") => 2
  case n if n.startsWith("wizard.model.player.PlayerTest") => 3
  case n if n.startsWith("wizard.model.player.PlayerFactory") => 4
  case n if n.startsWith("wizard.actionmanagement.Observer") => 5
  case n if n.startsWith("wizard.model.cards.Dealer") => 10
  case n if n.startsWith("wizard.actionmanagement.InputRouter") => 11
  case n if n.startsWith("wizard.model.player.Human") => 12
  case n if n.startsWith("wizard.model.player.AI") => 13
  case n if n.startsWith("wizard.controller.RoundState") => 20
  case n if n.startsWith("wizard.controller.PlayerLogic") => 21
  case n if n.startsWith("wizard.controller.RoundLogic") => 22
  case n if n.startsWith("wizard.controller.GameLogic") => 23
  case n if n.startsWith("wizard.controller.SpecialRules") => 23
  case n if n.contains("undo.Undo") => 24
  case n if n.contains("fileIo") || n.contains("FileIO") => 30
  case n if n.startsWith("wizard.aView.TextUI") => 40
  case n if n.startsWith("wizard.controller.GameIntegration") => 50
  case _ => 100
}

Test / testGrouping := {
  val tests = (Test / definedTests).value
  tests.map { test =>
    new sbt.Tests.Group(
      name = test.name,
      tests = Seq(test),
      runPolicy = sbt.Tests.SubProcess(sbt.ForkOptions())
    )
  }.sortBy(g => testOrder(g.name))
}

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

// Test / testOptions += Tests.Filter(_.equals("wizard.aTestSequence.TestSequence"))

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "versions", xs @ _*) => MergeStrategy.first
  case PathList("META-INF", xs @ _*) =>
    xs match {
      case "module-info.class" :: Nil => MergeStrategy.discard
      case "substrate" :: _ => MergeStrategy.first
      case _ => MergeStrategy.discard
    }
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}