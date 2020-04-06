name := "DynamoDBStreams-Connector"
organization := "com.goibibo"
version := "0.1"
scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-unused"
)


libraryDependencies ++= Seq(
    "org.apache.kafka" % "kafka-clients" % "0.11.0.0",
    "com.typesafe" % "config" % "1.2.1",
    "org.json4s" %% "json4s-jackson" % "3.2.10",
    "joda-time" % "joda-time" % "2.9.7",
    "log4j" % "log4j" % "1.2.17",
    "org.xerial" % "sqlite-jdbc" % "3.30.1",
    ("org.apache.zookeeper" % "zookeeper" % "3.4.5").
            exclude("javax.jms", "jms").
            exclude("com.sun.jdmk", "jmxtools").
            exclude("com.sun.jmx", "jmxri"),
    "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.714",
    "org.apache.commons" % "commons-lang3" % "3.0",
    "com.newrelic.agent.java" % "newrelic-api" % "3.33.0",
    "org.scalatest" %% "scalatest" % "3.0.5",
    "com.amazonaws" % "amazon-kinesis-client" % "1.13.3",
    "com.amazonaws" % "dynamodb-streams-kinesis-adapter" % "1.5.1"
)