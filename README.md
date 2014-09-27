finch-server
============

Some base classes and configuration used for making a server using finch

# Config Flags

```
  -io.github.benwhitehead.finch.adminHttpPort='9990': the TCP port for the admin http server
  -io.github.benwhitehead.finch.httpPort='7070': the TCP port for the http server
  -io.github.benwhitehead.finch.pidFile='': The file to write the pid of the process into
```

# Artifacts

Compiled for scala 2.10

## SBT

```
resolvers += "BenWhitehead" at "http://storage.googleapis.com/benwhitehead_me/maven/public"

libraryDependencies ++= Seq(
  "io.github.benwhitehead.finch" %% "finch-server" % "0.2.1"
)
```
