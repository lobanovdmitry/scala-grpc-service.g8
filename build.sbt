import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "ru.dlobanov"
ThisBuild / organizationName := "dlobanov"

lazy val api = (project in file("api"))
  .settings(
    name := "scala-grpc-service-api",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      scalaTest % Test
    ),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    )
  )

lazy val root = (project in file("server"))
  .dependsOn(api)
  .settings(
    name := "scala-grpc-service-template",
    libraryDependencies ++= `scala-logging`
  )
