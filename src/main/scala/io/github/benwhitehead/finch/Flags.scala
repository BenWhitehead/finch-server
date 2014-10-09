package io.github.benwhitehead.finch

import com.twitter.app.GlobalFlag

object adminHttpPort extends GlobalFlag[Int](9990, "the TCP port for the admin http server")
object httpPort extends GlobalFlag[Int](7070, "the TCP port for the http server")
object pidFile extends GlobalFlag[String]("", "The file to write the pid of the process into")
object httpsPort extends GlobalFlag[Int](7443, "the TCP port for the https server")
object certificatePath extends GlobalFlag[String]("", "Path to PEM format SSL certificate file")
object keyPath extends GlobalFlag[String]("", "Path to SSL Key file")
object maxRequestSize extends GlobalFlag[Int](5, "Max request size (in megabytes)")
object decompressionEnabled extends GlobalFlag[Boolean](false, "Enables deflate,gzip Content-Encoding handling")
object compressionLevel extends GlobalFlag[Int](6, "Enables deflate,gzip Content-Encoding handling")
