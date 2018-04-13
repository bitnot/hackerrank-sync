import sbt._

object Dependencies {

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-java8"
  ).map(_ % Versions.circeVersion)
  val commonDeps = Seq(
    "com.typesafe" % "config" % Versions.typesafeConfig
  )
  val sttp = Seq(
    "com.softwaremill.sttp" %% "core" % Versions.sttp,
    "com.softwaremill.sttp" %% "circe" % Versions.sttp
  )
  val ScalaTest = "org.scalatest" %% "scalatest" % Versions.scalatest
  val ScalaMock = "org.scalamock" %% "scalamock" % Versions.scalamock

  object Versions {
    val sttp = "1.1.12"
    val scalatest = "3.0.5"
    val scalamock = "4.1.0"
    val typesafeConfig = "1.3.1"
    val circeVersion = "0.9.3"
  }

}
