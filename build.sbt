import Dependencies._

name := "hkwget"
organization := "org.bitnot"
version := "0.0.2"
scalaVersion := "2.12.12"

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.typesafeIvyRepo("releases"),
  Resolver.typesafeIvyRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= circe
libraryDependencies ++= commonDeps
libraryDependencies ++= sttp

libraryDependencies ++= Seq(scalaTest, scalaMock) map (_ % Test)
