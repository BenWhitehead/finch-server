finch-server
============

Finch Server is a library that merges together the great libraries [finch](https://github.com/finagle/finch), and [twitter-server](https://github.com/twitter/twitter-server).

Twitter has done a great job of providing great features in twitter-server, but there are some subtleties in utilizing these features, this project aims to made it as easy as possible to use finch and twitter-server together.

# Features

## Config Flags

All confiurable aspects of a finch-server can be configured with command line flags using twitter-utils [Flag](https://github.com/twitter/util/blob/master/util-app/src/main/scala/com/twitter/app/Flag.scala). The following is a list of flags that are available to configure a finch-server.

The motivation behind using flags is that when running applications on Mesos or in the Cloud, having a self contained task is much better than depending on other resource to configure your app (such as properties/config files).  By having all configuration take place with command line flags it becomes trivial to run your server on any host.

```
-admin.port=:9990                                         : the TCP port for the admin http server
-io.github.benwhitehead.finch.certificatePath=            : Path to PEM format SSL certificate file
-io.github.benwhitehead.finch.httpPort=7070               : the TCP port for the http server
-io.github.benwhitehead.finch.httpsPort=7443              : the TCP port for the https server
-io.github.benwhitehead.finch.keyPath=                    : Path to SSL Key file
-io.github.benwhitehead.finch.maxRequestSize=5            : Max request size (in megabytes)
-io.github.benwhitehead.finch.pidFile=                    : The file to write the pid of the process into
-io.github.benwhitehead.finch.accessLog=access-log        : Whether to add an Access Log Filter, and if so which type [off|access-log{default}|access-log-combined]. Any value other than the listed 3 will be treated as off.
```

# Filters

## Exception Handling
Finch-server provides a default exception handler that will handle any exception that your application hasn't handled. When an unhandled exception is encountered a metric will be incremented for the configured StatsReceiver the server boots with.

This allows for easier monitoring of the number of occurrences of particular exceptions in your application.

## Route Histograms
Finch Proves a great API for declaring route handlers, finch-server adds a filter to the server to record latency and request count per route and report all metrics to the configured StatsReceiver.

## Access Logging
Finch-server can weave a filter into the request chain to provide access logging for all requests to the server. Both traditional and combined formats can be chosen from.

# Admin HTTP Server
Twitter-server provides a great AdminHttpServer that can be used to gain insight into how your application is performing, or to provide important administrative tasks (like getting a thread dump from a running server through an Http Interface).  There can be some nuance to correctly starting the AdminHttpServer so this is done automatically by finch-server.

# SLF4J Logging
By default all twitter libraries use Java Util Logging, finch-server has been configured to use SLF4J for all logging and automatically sets up all SLF4J bridges and re-configures Java Util Logging.

No SLF4J Backed is declared as a dependency, so feel free to pick wichever backend you like.  The unit tests however use logback.

An example `logback.xml` for how to configure access-log file and rolling is available in `example-logback.xml` in the repo or bundled in the jar.

# Usage
finch-server is very easy to use, all you need to create an echo server are the two following components.  With these two components you will now have a full server running your application on port 7070 and the Admin Server on 9990.

### Finch Endpoint
```scala
object Echo extends HttpEndpoint {
  def service(echo: String) = new Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(echo).toFuture
    }
  }
  def route = {
    case Method.Get -> Root / "echo" / echo => service(echo)
  }
}
```

### Server Object
```scala
object TestingServer extends SimpleFinchServer {
  override lazy val serverName = "echo"
  def endpoint = Echo
}
```

The Endpoint will be boostrapped with all filters and can service requests. For example:
```
GET /echo/Hello+World HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:17070
User-Agent: HTTPie/0.9.0

HTTP/1.1 200 OK
Content-Length: 11

Hello World

```

# Artifacts

Compiled for scala 2.10 and 2.11

## SBT

Artifacts for finch-server are currently hosed in a google storage bucket, so you will need to add a resolver to your sbt config.

```
resolvers += "finch-server" at "http://storage.googleapis.com/benwhitehead_me/maven/public"

libraryDependencies ++= Seq(
  "io.github.benwhitehead.finch" %% "finch-server" % "0.7.2"
)
```
