finch-server
============

Some base classes and configuration used for making a server using finch

# Config Flags

```
  -io.github.benwhitehead.finch.adminHttpPort=9990          : the TCP port for the admin http server
  -io.github.benwhitehead.finch.certificatePath=            : Path to PEM format SSL certificate file
  -io.github.benwhitehead.finch.httpPort=7070               : the TCP port for the http server
  -io.github.benwhitehead.finch.httpsPort=7443              : the TCP port for the https server
  -io.github.benwhitehead.finch.keyPath=                    : Path to SSL Key file
  -io.github.benwhitehead.finch.maxRequestSize=5            : Max request size (in megabytes)
  -io.github.benwhitehead.finch.pidFile=                    : The file to write the pid of the process into
  -io.github.benwhitehead.finch.decompressionEnabled=false  : Enables deflate,gzip Content-Encoding handling
  -io.github.benwhitehead.finch.compressionLevel=6          : Enables deflate,gzip Content-Encoding handling
```

# Artifacts

Compiled for scala 2.10

## SBT

```
resolvers += "BenWhitehead" at "http://storage.googleapis.com/benwhitehead_me/maven/public"

libraryDependencies ++= Seq(
  "io.github.benwhitehead.finch" %% "finch-server" % "0.5.0"
)
```
