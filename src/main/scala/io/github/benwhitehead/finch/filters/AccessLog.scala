package io.github.benwhitehead.finch.filters

import java.text.SimpleDateFormat
import java.util.Date

import com.twitter.finagle.http.Version.{Http10, Http11}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.finagle.http.{Version, Response, Request}
import com.twitter.util.Future

class AccessLog(combined: Boolean) extends SimpleFilter[Request, Response] {
  lazy val accessLog = org.slf4j.LoggerFactory.getLogger("access-log")
  def apply(request: Request, service: Service[Request, Response]) = {
    if (accessLog.isTraceEnabled) {
      service(request) flatMap { case resp =>
        val reqHeaders = request.headerMap
        val remoteHost = request.remoteHost
        val identd = "-"
        val user = "-"
        val requestEndTime = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss Z").format(new Date())
        val reqResource = s"${request.method.toString.toUpperCase} ${request.uri} ${versionString(request.version)}"
        val statusCode = resp.statusCode
        val responseBytes = resp.headerMap.getOrElse("Content-Length", "-")

        if (!combined) {
          accessLog.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s""")
        } else {
          val referer = reqHeaders.getOrElse("Referer", "-")
          val userAgent = reqHeaders.getOrElse("User-Agent", "-")
          accessLog.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s "$referer%s" "$userAgent%s"""")
        }
        Future.value(resp)
      }
    } else {
      service(request)
    }
  }

  def versionString(v: Version) = v match {
    case Http11 => "HTTP/1.1"
    case Http10 => "HTTP/1.0"
  }
}
object AccessLog {
  lazy val accessLog = org.slf4j.LoggerFactory.getLogger("access-log")
  type httpFilter = Filter[Request, Response, Request, Response]
  def apply(preFilter: httpFilter, logType: String): httpFilter = {
    logType match {
      case "access-log" => preFilter andThen new AccessLog(false)
      case "access-log-combined" => preFilter andThen new AccessLog(true)
      case _ =>
        accessLog.trace("access log disabled")
        preFilter
    }
  }
}
