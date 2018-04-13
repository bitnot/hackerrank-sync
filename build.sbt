import Dependencies._

name := "hkwget"
organization := "org.bitnot"
version := "0.0.1"
scalaVersion := "2.12.4"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= circe
libraryDependencies ++= commonDeps
libraryDependencies ++= sttp

libraryDependencies ++= Seq(ScalaTest, ScalaMock) map (_ % Test)