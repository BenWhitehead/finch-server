package io.github.benwhitehead.finch

import com.twitter.app.GlobalFlag

object httpPort extends GlobalFlag[Int](7070, "the TCP port for the http server")
object pidFile extends GlobalFlag[String]("", "The file to write the pid of the process into")
object httpsPort extends GlobalFlag[Int](7443, "the TCP port for the https server")
object certificatePath extends GlobalFlag[String]("", "Path to PEM format SSL certificate file")
object keyPath extends GlobalFlag[String]("", "Path to SSL Key file")
object maxRequestSize extends GlobalFlag[Int](5, "Max request size (in megabytes)")
// TODO: Figure out how to add back compression support
//object decompressionEnabled extends GlobalFlag[Boolean](false, "Enables deflate,gzip Content-Encoding handling")
//object compressionLevel extends GlobalFlag[Int](6, "Enables deflate,gzip Content-Encoding handling")
object accessLog extends GlobalFlag[String]("access-log", "Whether to add an Access Log Filter, and if so which type [off|access-log{default}|access-log-combined]. Any value other than the listed 3 will be treated as off.")
