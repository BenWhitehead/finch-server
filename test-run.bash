#!/bin/bash


function broken {
  CLASSPATH=""
  CLASSPATH="$CLASSPATH:$(pwd)/target/scala-2.10/classes"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/twitter-server_2.10/jars/twitter-server_2.10-1.7.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-app_2.10/jars/util-app_2.10-6.19.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-core_2.10/jars/util-core_2.10-6.19.0.jar"

  # broke
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-core_2.10/jars/finagle-core_2.10-6.20.0.jar"

  run ${CLASSPATH}
}

function minWorking {
  CLASSPATH=""
  CLASSPATH="$CLASSPATH:$(pwd)/target/scala-2.10/classes"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/twitter-server_2.10/jars/twitter-server_2.10-1.7.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-app_2.10/jars/util-app_2.10-6.19.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-core_2.10/jars/util-core_2.10-6.19.0.jar"

  run ${CLASSPATH}
}

function run {
  java -classpath ${1} io.github.benwhitehead.finch.Main -help
}

function main {
  echo "should work"
  minWorking
  echo "should break"
  broken
}

main
