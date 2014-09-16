package io.github.benwhitehead.finch

import com.twitter.app.GlobalFlag

object adminHttpPort extends GlobalFlag[Int](9990, "the TCP port for the admin http server")
object httpPort extends GlobalFlag[Int](7070, "the TCP port for the http server")
object pidFile extends GlobalFlag[String]("", "The file to write the pid of the process into")
