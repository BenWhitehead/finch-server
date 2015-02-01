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
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.server.Stats
import com.twitter.util.Future
import io.finch.request.{ParamNotFound, ValidationFailed}
import io.finch.{response, _}

import java.text.SimpleDateFormat
import java.util.Date

package object filters {

  object HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      import io.finch.json._
      service(request) handle {
        case e: ValidationFailed     => response.BadRequest(Json.obj("message" -> e.getMessage))
        case e: ParamNotFound        => response.BadRequest(Json.obj("message" -> e.getMessage))
        case e: RespondWithException => e.response
        case e: BadRequest           => response.BadRequest()
        case e: Unauthorized         => response.Unauthorized()
        case e: PaymentRequired      => response.PaymentRequired()
        case e: Forbidden            => response.Forbidden()
        case e: NotFound             => response.NotFound()
        case e: MethodNotAllowed     => response.MethodNotAllowed()
        case e: NotAcceptable        => response.NotAcceptable()
        case e: RequestTimeOut       => response.RequestTimeOut()
        case e: Conflict             => response.Conflict()
        case e: PreconditionFailed   => response.PreconditionFailed()
        case e: TooManyRequests      => response.TooManyRequests()
        case e: NotImplemented       => response.NotImplemented()
        case e: InternalServerError  => response.InternalServerError()
        case e: BadGateway           => response.BadGateway()
        case e: ServiceUnavailable   => response.ServiceUnavailable()
        case t: Throwable            => logger.error("", t); response.InternalServerError()
      }
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

  object AccessLog extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val common = org.slf4j.LoggerFactory.getLogger("access-log")
    lazy val combined = org.slf4j.LoggerFactory.getLogger("access-log-combined")
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      if (common.isTraceEnabled || combined.isTraceEnabled) {
        service(request) flatMap { case resp =>
          val reqHeaders = request.headerMap
          val remoteHost = request.remoteHost
          val identd = "-"
          val user = "-"
          val requestEndTime = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss Z").format(new Date())
          val reqResource = s"${request.method.toString.toUpperCase} ${request.uri} ${versionString(request.version)}"
          val statusCode = resp.statusCode
          val responseBytes = resp.headerMap.getOrElse("Content-Length", "-")

          if (common.isTraceEnabled) {
            common.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s""")
          }
          if (combined.isTraceEnabled) {
            val referer = reqHeaders.getOrElse("Referer", "-")
            val userAgent = reqHeaders.getOrElse("User-Agent", "-")
            combined.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s "$referer%s" "$userAgent%s"""")
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


}
