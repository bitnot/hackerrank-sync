import sbt._

object Dependencies {

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % Versions.circeVersion)

  val commonDeps = Seq(
    "com.typesafe" % "config" % Versions.typesafeConfig,
    "ch.qos.logback" % "logback-classic" % Versions.logback,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  )

  val scalaMock = "org.scalamock" %% "scalamock" % Versions.scalamock
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalatest

  val sttp = Seq(
    "com.softwaremill.sttp" %% "core" % Versions.sttp,
    "com.softwaremill.sttp" %% "circe" % Versions.sttp
  )

  object Versions {
    val circeVersion = "0.12.1"
    val logback = "1.2.3"
    val scalamock = "4.1.0"
    val scalatest = "3.0.5"
    val sttp = "1.7.2"
    val typesafeConfig = "1.3.1"
  }

}
