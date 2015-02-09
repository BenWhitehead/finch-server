/*
 * Copyright (c) 2014 Ben Whitehead.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.benwhitehead.finch

import com.twitter.app.{App => TApp}
import com.twitter.finagle.httpx.Version
import com.twitter.finagle.httpx.Version.{Http10, Http11}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.server.Stats
import com.twitter.util.Future
import io.finch.request.{ParamNotFound, ValidationFailed}
import io.finch.{response, _}

import java.text.SimpleDateFormat
import java.util.Date

package object filters {

  class HandleExceptions(baseScope: String = "") extends SimpleFilter[HttpRequest, HttpResponse] with TApp with Stats {
    lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)
    val stats = {
      if (baseScope.nonEmpty) statsReceiver.scope(s"$baseScope/error")
      else statsReceiver.scope("error")
    }
    val exceptionScope = {
      if (baseScope.nonEmpty) statsReceiver.scope(s"$baseScope/exception")
      else statsReceiver.scope("exception")
    }
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      import io.finch.json._
      service(request) handle {
        case e: ValidationFailed     => countStatus(400); response.BadRequest(Json.obj("message" -> e.getMessage))
        case e: ParamNotFound        => countStatus(400); response.BadRequest(Json.obj("message" -> e.getMessage))
        case e: RespondWithException => countStatus(e); e.response
        case e: BadRequest           => countStatus(e); response.BadRequest()
        case e: Unauthorized         => countStatus(e); response.Unauthorized()
        case e: PaymentRequired      => countStatus(e); response.PaymentRequired()
        case e: Forbidden            => countStatus(e); response.Forbidden()
        case e: NotFound             => countStatus(e); response.NotFound()
        case e: MethodNotAllowed     => countStatus(e); response.MethodNotAllowed()
        case e: NotAcceptable        => countStatus(e); response.NotAcceptable()
        case e: RequestTimeOut       => countStatus(e); response.RequestTimeOut()
        case e: Conflict             => countStatus(e); response.Conflict()
        case e: PreconditionFailed   => countStatus(e); response.PreconditionFailed()
        case e: TooManyRequests      => countStatus(e); response.TooManyRequests()
        case e: NotImplemented       => countStatus(e); response.NotImplemented()
        case e: InternalServerError  => countStatus(e); response.InternalServerError()
        case e: BadGateway           => countStatus(e); response.BadGateway()
        case e: ServiceUnavailable   => countStatus(e); response.ServiceUnavailable()
        case t: Throwable            =>
          countStatus(500)
          exceptionScope.counter(t.getClass.getName).incr()
          logger.error("", t)
          response.InternalServerError()
      }
    }

    def countStatus(status: Int): Unit = {
      stats.counter(s"$status").incr()
    }
    def countStatus(t: RespondWithException): Unit = {
      countStatus(t.response.getStatusCode())
    }
    def countStatus(t: HttpException): Unit = {
      countStatus(t.status)
    }
  }

  class StatsFilter(baseScope: String = "") extends SimpleFilter[HttpRequest, HttpResponse] with TApp with Stats {
    val stats = {
      if (baseScope.nonEmpty) statsReceiver.scope(s"$baseScope/route")
      else statsReceiver.scope("route")
    }
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val label = s"${request.method}/Root/${request.path.stripPrefix("/")}"
      stats.timeFuture(label) {
        val f = service(request)
        stats.counter(label).incr()
        f
      }
    }
  }

  class AccessLog(combined: Boolean) extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val accessLog = org.slf4j.LoggerFactory.getLogger("access-log")
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
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
          resp.toFuture
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
    type httpFilter = Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]
    def apply(preFilter: httpFilter, logType: String): httpFilter = {
      logType match {
        case "access-log" => preFilter ! new AccessLog(false)
        case "access-log-combined" => preFilter ! new AccessLog(true)
        case _ => preFilter
      }
    }
  }


}
