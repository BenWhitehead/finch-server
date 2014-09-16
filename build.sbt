organization := "io.github.benwhitehead.finch"

name := "finch-server"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

javacOptions in doc := Seq("-source", "1.7")

resolvers += "Twitter" at "http://maven.twttr.com/"

resolvers += "Finch.io" at "http://repo.konfettin.ru"

libraryDependencies ++= Seq(
  "io"              %% "finch"              % "0.1.6",
  "com.twitter"     %% "twitter-server"     % "1.7.3",
  "org.slf4j"       %  "slf4j-api"          % "1.7.7",
  "org.slf4j"       %  "jul-to-slf4j"       % "1.7.7",
  "org.slf4j"       %  "jcl-over-slf4j"     % "1.7.7",
  "org.slf4j"       %  "log4j-over-slf4j"   % "1.7.7",
  "ch.qos.logback"  %  "logback-classic"    % "1.1.2"   % "test",
  "org.scalatest"   %% "scalatest"          % "2.2.2"   % "test"
)

parallelExecution in Test := false

