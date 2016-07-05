package io.github.benwhitehead.finch

import java.net.InetSocketAddress

import com.twitter.app.{Flaggable, GlobalFlag}

private[finch] object Flaggables {
  implicit def flagOfOption[T](implicit flaggable: Flaggable[T]) = new Flaggable[Option[T]] {
    override def parse(s: String): Option[T] = {
      Option(s)
        .map(_.trim)
        .collect {
          case x if x.nonEmpty => x
        }
        .map(flaggable.parse)
    }

    override def show(t: Option[T]): String = t match {
      case Some(tt) => flaggable.show(tt)
      case None => ""
    }
  }
}

object pidFile extends GlobalFlag[String]("", "The file to write the pid of the process into")
object certificatePath extends GlobalFlag[String]("", "Path to PEM format SSL certificate file")
object keyPath extends GlobalFlag[String]("", "Path to SSL Key file")
object maxRequestSize extends GlobalFlag[Int](5, "Max request size (in megabytes)")
object accessLog extends GlobalFlag[String]("access-log", "Whether to add an Access Log Filter, and if so which type [off|access-log{default}|access-log-combined]. Any value other than the listed 3 will be treated as off.")

object httpInterface extends GlobalFlag[Option[InetSocketAddress]](
  Some(new InetSocketAddress("0.0.0.0", 7070)),
  s"The TCP Interface and port for the http server {[<hostname/ip>]:port}. (Set to empty value to disable)"
)(Flaggables.flagOfOption)
object httpsInterface extends GlobalFlag[Option[InetSocketAddress]](
  None,
  s"The TCP Interface and port for the https server {[<hostname/ip>]:port}. Requires -${certificatePath.name} and -${keyPath.name} to be set. (Set to empty value to disable)"
)(Flaggables.flagOfOption)
