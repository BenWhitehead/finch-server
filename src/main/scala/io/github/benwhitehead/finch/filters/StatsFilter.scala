package io.github.benwhitehead.finch.filters

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.stats.{Stat, StatsReceiver}
import com.twitter.util.Future

class StatsFilter(baseScope: String = "")(implicit statsReceiver: StatsReceiver) extends SimpleFilter[Request, Response] {
  val stats = {
    if (baseScope.nonEmpty) statsReceiver.scope(s"$baseScope/route")
    else statsReceiver.scope("route")
  }
  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val label = s"${request.method.toString.toUpperCase}/ROOT/${request.path.stripPrefix("/")}"
    Stat.timeFuture(stats.stat(label)) {
      val f = service(request)
      stats.counter(label).incr()
      f
    }
  }
}
