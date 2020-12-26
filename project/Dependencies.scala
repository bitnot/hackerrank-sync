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

   val resilience4j = Seq(
     "io.github.resilience4j" % "resilience4j-ratelimiter" % Versions.resilience4j
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
    val resilience4j = "1.6.1"
    val scalaLogging = "3.9.2"
    val scalamock = "5.1.0"
    val scalatest = "3.2.3"
    val sttp = "2.2.9"
    val typesafeConfig = "1.4.1"
  }

}
