import sbt._

object Dependencies {

  lazy val `scala-logging` = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1"
}
