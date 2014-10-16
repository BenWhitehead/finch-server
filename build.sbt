 // put this at the top of the file

organization := "io.github.benwhitehead.finch"

name := "finch-server"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

javacOptions in doc := Seq("-source", "1.7")

resolvers += "Twitter" at "http://maven.twttr.com/"

libraryDependencies ++= Seq(
  "com.twitter"     %% "twitter-server"     % "1.7.3",
  "com.twitter"     %% "finagle-core"       % "6.22.0"
)

parallelExecution in Test := false

assemblySettings
