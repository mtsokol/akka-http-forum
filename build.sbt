name := "akka-http-forum"

version := "0.1"

scalaVersion := "2.12.5"

enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

libraryDependencies ++= {
  val akkaVersion       = "2.5.11"
  val akkaHttpVersion   = "10.1.0"
  val slickVersion = "3.2.2"
  Seq(
    "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.postgresql" % "postgresql" % "42.1.4",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}
