name := """akka-streams-logreader"""

organization := "ch.taggiasco"

version := "0.0.1"

scalaVersion := "2.12.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion     = "2.4.16"

  Seq(
    "com.typesafe.akka" %% "akka-stream"         % akkaVersion,
    "org.scalatest"     %% "scalatest"           % "3.0.1"     % "test",
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  )
}
