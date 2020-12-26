import Dependencies._
import cats.instances.map

name := "hkwget"
organization := "org.bitnot"
version := "0.0.2"

scalaVersion := "3.0.0-M3"
//crossScalaVersions ++= Seq("2.13.4", "3.0.0-M3")

scalacOptions ++= {
  if (isDotty.value) Seq(
    "-source:3.0-migration",
    "-rewrite")
  else Seq.empty
}

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.typesafeIvyRepo("releases"),
  Resolver.typesafeIvyRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= circe
libraryDependencies ++= commonDeps
libraryDependencies ++= logging
libraryDependencies ++= resilience4j
libraryDependencies ++= sttp
  .map(_.withDottyCompat(scalaVersion.value))
  .map(_
    .exclude("io.circe", "circe-core_2.13")
    .exclude("io.circe", "circe-jawn_2.13")
    .exclude("io.circe", "circe-numbers_2.13")
    .exclude("io.circe", "circe-parser_2.13")
    .exclude("org.typelevel", "cats-core_2.13")
    .exclude("org.typelevel", "cats-kernel_2.13")
    .exclude("org.typelevel", "jawn-parser_2.13")
  )

// TODO #2
libraryDependencies ++= Seq(scalaTest, scalaMock) map (_ % Test) map (_.withDottyCompat(scalaVersion.value))
