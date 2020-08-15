import Dependencies._

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  organization := "ru.dlobanov",
  scalaVersion := "2.13.2",
  test in assembly := {}
)

lazy val api = (project in file("api"))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-grpc-service-api",
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
    name := "scala-grpc-server",
    libraryDependencies ++= `scala-logging` ++ grpcNetty,
    mainClass in assembly := Some("com.example.Main"),
    assemblyJarName in assembly := "grpc-server.jar"
  )

lazy val root = (project in file("."))
  .aggregate(api, server)
  .disablePlugins(AssemblyPlugin)
