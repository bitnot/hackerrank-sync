import Dependencies._

name := "hkwget"
organization := "org.bitnot"
version := "0.0.2"

scalaVersion := "3.0.0-M3"
//crossScalaVersions ++= Seq("2.13.4", "3.0.0-M3")

scalacOptions ++= {
  if (isDotty.value) Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked")
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
  .map(_.excludeAll(
    ExclusionRule(organization = "io.circe"),
    ExclusionRule(organization = "org.typelevel"))
  )

libraryDependencies ++= Seq(scalaTest) map (_ % Test)
