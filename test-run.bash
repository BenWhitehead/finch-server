#!/bin/bash


function broken {
  CLASSPATH=""
  CLASSPATH="$CLASSPATH:$(pwd)/target/scala-2.10/classes"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/twitter-server_2.10/jars/twitter-server_2.10-1.7.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-app_2.10/jars/util-app_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-core_2.10/jars/util-core_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-jvm_2.10/jars/util-jvm_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-core_2.10/jars/finagle-core_2.10-6.22.0.jar"

  # broke
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-http_2.10/jars/finagle-http_2.10-6.22.0.jar"

  run ${CLASSPATH}
}

function minWorking {
  CLASSPATH=""
  CLASSPATH="$CLASSPATH:$(pwd)/target/scala-2.10/classes"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/twitter-server_2.10/jars/twitter-server_2.10-1.7.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-app_2.10/jars/util-app_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-core_2.10/jars/util-core_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-jvm_2.10/jars/util-jvm_2.10-6.22.0.jar"

  run ${CLASSPATH}
}

function maxWorking {
  CLASSPATH="$CLASSPATH:$(pwd)/target/scala-2.10/classes"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.10.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/twitter-server_2.10/jars/twitter-server_2.10-1.7.3.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/io.netty/netty/bundles/netty-3.9.4.Final.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-app_2.10/jars/util-app_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-core_2.10/jars/util-core_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-collection_2.10/jars/util-collection_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/javax.inject/javax.inject/jars/javax.inject-1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.google.guava/guava/bundles/guava-16.0.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/commons-collections/commons-collections/jars/commons-collections-3.2.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-hashing_2.10/jars/util-hashing_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-jvm_2.10/jars/util-jvm_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-logging_2.10/jars/util-logging_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scalatest/scalatest_2.10/bundles/scalatest_2.10-2.2.2.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.10.4.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/util-codec_2.10/jars/util-codec_2.10-6.19.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/commons-codec/commons-codec/jars/commons-codec-1.6.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/commons-lang/commons-lang/jars/commons-lang-2.6.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.apache.thrift/libthrift/jars/libthrift-0.5.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/org.slf4j/slf4j-api/jars/slf4j-api-1.5.8.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/scrooge-core_2.10/jars/scrooge-core_2.10-3.16.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.github.spullara.mustache.java/compiler/bundles/compiler-0.8.12.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/bundles/jackson-core-2.3.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-databind/bundles/jackson-databind-2.3.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.fasterxml.jackson.core/jackson-annotations/bundles/jackson-annotations-2.3.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-scala_2.10/bundles/jackson-module-scala_2.10-2.3.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.thoughtworks.paranamer/paranamer/jars/paranamer-2.6.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.google.code.findbugs/jsr305/jars/jsr305-2.0.1.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-core_2.10/jars/finagle-core_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-zipkin_2.10/jars/finagle-zipkin_2.10-6.22.0.jar"
  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-thrift_2.10/jars/finagle-thrift_2.10-6.22.0.jar"

  CLASSPATH="$CLASSPATH:$HOME/.ivy2/cache/com.twitter/finagle-http_2.10/jars/finagle-http_2.10-6.22.0.jar"

  run ${CLASSPATH}
}

function run {
  java -classpath ${1} io.github.benwhitehead.finch.Main -help
}

function main {
  echo "min working"
  minWorking
  echo "max working"
  maxWorking
  echo "should break"
  broken
}


######################### Delegates to subcommands or runs main, as appropriate
if [[ ${1:-} ]] && declare -F | cut -d' ' -f3 | fgrep -qx -- "${1:-}"
then "$@"
else main
fi
