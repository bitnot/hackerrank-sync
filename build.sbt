import Dependencies._
import cats.instances.map

name := "hkwget"
organization := "org.bitnot"
version := "0.0.2"
scalaVersion := "2.13.4"

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.typesafeIvyRepo("releases"),
  Resolver.typesafeIvyRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= circe.map(_.withDottyCompat(scalaVersion.value))
libraryDependencies ++= commonDeps.map(_.withDottyCompat(scalaVersion.value))
libraryDependencies ++= resilience4j.map(_.withDottyCompat(scalaVersion.value))
libraryDependencies ++= sttp.map(_.withDottyCompat(scalaVersion.value))

libraryDependencies ++= Seq(scalaTest, scalaMock) map (_ % Test) map (_.withDottyCompat(scalaVersion.value))
