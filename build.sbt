import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "Booking"
  )

val http4sVersion = "0.23.30"
val mongo4cats = "0.7.13"
val CirisVersion = "3.10.0"
val CirceVersion = "0.14.14"
val doobieVersion = "1.0.0-RC10"


libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "is.cir" %% "ciris" % CirisVersion,
  "is.cir" %% "ciris-circe" % CirisVersion,
  "io.circe" %% "circe-literal" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
)