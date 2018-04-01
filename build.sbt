name := "akka-http-forum"

version := "0.1"

scalaVersion := "2.12.5"

enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0",
  "com.typesafe.slick" %% "slick" % "3.2.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
