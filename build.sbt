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