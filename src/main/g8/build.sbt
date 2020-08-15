import Dependencies._

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  organization := "$organization$",
  scalaVersion := "2.13.2",
  test in assembly := {}
)

lazy val api = (project in file("api"))
  .settings(commonSettings: _*)
  .settings(
    name := "$name;format="normalize"$-api",
    libraryDependencies ++= scalaPb ++ Seq(
      scalaTest % Test
    ),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    )
  ).disablePlugins(AssemblyPlugin)

lazy val server = (project in file("server"))
  .dependsOn(api)
  .aggregate(api)
  .settings(commonSettings: _*)
  .settings(
    name := "$name;format="normalize"$-server",
    libraryDependencies ++= `scala-logging` ++ grpcNetty ++ Seq(scalaTest % Test),
    mainClass in assembly := Some("$package$.server.App"),
    assemblyJarName in assembly := "$name;format="normalize"$.jar"
  )

lazy val root = (project in file("."))
  .aggregate(api, server)
  .disablePlugins(AssemblyPlugin)
