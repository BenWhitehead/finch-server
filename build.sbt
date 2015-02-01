organization := "io.github.benwhitehead.finch"

name := "finch-server"

version := "0.7.1-SNAPSHOT"

crossScalaVersions := Seq("2.10.3", "2.11.4")

scalacOptions ++= Seq("-unchecked", "-deprecation")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

javacOptions in doc := Seq("-source", "1.7")

resolvers += "Twitter" at "http://maven.twttr.com/"

libraryDependencies ++= Seq(
  "com.github.finagle"  %% "finch-core"         % "0.4.0",
  "com.github.finagle"  %% "finch-json"         % "0.4.0",
  "com.github.finagle"  %% "finch-jackson"      % "0.4.0"   % "test",
  "com.twitter"         %% "finagle-stats"      % "6.24.0",
  "com.twitter"         %% "finagle-httpx"      % "6.24.0",
  "com.twitter"         %% "twitter-server"     % "1.9.0",
  "org.slf4j"           %  "slf4j-api"          % "1.7.10",
  "org.slf4j"           %  "jul-to-slf4j"       % "1.7.10",
  "org.slf4j"           %  "jcl-over-slf4j"     % "1.7.10",
  "org.slf4j"           %  "log4j-over-slf4j"   % "1.7.10",
  "ch.qos.logback"      %  "logback-classic"    % "1.1.2"   % "test",
  "org.scalatest"       %% "scalatest"          % "2.2.2"   % "test"
)

parallelExecution in Test := false

net.virtualvoid.sbt.graph.Plugin.graphSettings
