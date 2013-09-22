name := """soap-circuit-breaker-akka"""

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
  "com.typesafe.akka" %% "akka-camel" % "2.2.1",
  "org.apache.camel" % "camel-cxf" % "2.11.1",
  "org.apache.cxf" % "cxf-rt-transports-http-jetty" % "2.7.6",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test",
  "org.apache.camel" % "camel-jetty" % "2.11.1" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")