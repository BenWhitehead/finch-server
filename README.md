finch-server
============

Finch Server is a library that merges together the great libraries [finch](https://github.com/finagle/finch), and [twitter-server](https://github.com/twitter/twitter-server).

Twitter has done a great job of providing great features in twitter-server, but there are some subtleties in utilizing these features, this project aims to made it as easy as possible to use finch and twitter-server together.

# Features

There are flags setup to configure all parameters for the servers to allow for external configuration
there is a default exception handler that will create responses for certain exception types
it automatically sets up per-route stats for you so that you can get histogram data for all routes that are accessed in your server
there is a filter that gets woven into the setup to provide access logging
the twitter admin http server is automatically started for you
the access logging is controlled by a separate appender that can be configured independent from the application logging

# Config Flags

```
  -admin.port=:9990                                         : the TCP port for the admin http server
  -io.github.benwhitehead.finch.certificatePath=            : Path to PEM format SSL certificate file
  -io.github.benwhitehead.finch.httpPort=7070               : the TCP port for the http server
  -io.github.benwhitehead.finch.httpsPort=7443              : the TCP port for the https server
  -io.github.benwhitehead.finch.keyPath=                    : Path to SSL Key file
  -io.github.benwhitehead.finch.maxRequestSize=5            : Max request size (in megabytes)
  -io.github.benwhitehead.finch.pidFile=                    : The file to write the pid of the process into
```

# Artifacts

Compiled for scala 2.10 and 2.11

## SBT

```
resolvers += "BenWhitehead" at "http://storage.googleapis.com/benwhitehead_me/maven/public"

libraryDependencies ++= Seq(
  "io.github.benwhitehead.finch" %% "finch-server" % "0.7.1"
)
```
