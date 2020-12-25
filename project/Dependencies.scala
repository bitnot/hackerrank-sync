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
    "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
  )

  val scalaMock = "org.scalamock" %% "scalamock" % Versions.scalamock
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalatest

  val sttp = Seq(
    "com.softwaremill.sttp.client" %% "core" % Versions.sttp
    ,"com.softwaremill.sttp.client" %% "circe" % Versions.sttp
  )

  object Versions {
    val circeVersion = "0.13.0"
    val logback = "1.2.3"
    val scalaLogging = "3.9.2"
    val scalamock = "5.0.0"
    val scalatest = "3.2.1"
    val sttp = "2.2.4"
    val typesafeConfig = "1.4.0"
  }

}
