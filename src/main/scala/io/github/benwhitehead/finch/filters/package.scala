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

import java.text.SimpleDateFormat
import java.util.Date

import com.twitter.app.{App => TApp}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.server.Stats
import com.twitter.util.Future
import io.finch.json.JsonObject
import io.finch.request.{ParamNotFound, ValidationFailed}
import io.finch.{response, _}

package object filters {

  object HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      service(request) handle {
        case e: ValidationFailed     => response.BadRequest(JsonObject("message" -> e.getMessage))
        case e: ParamNotFound        => response.BadRequest(JsonObject("message" -> e.getMessage))
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
        case e: BadGateway           => BadGateway()
        case e: ServiceUnavailable   => ServiceUnavailable()
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
      stats.timeFuture(s"${request.method}/Root/${request.path.stripPrefix("/")}") {
        service(request)
      }
    }
  }

  object AccessLog extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val common = org.slf4j.LoggerFactory.getLogger("access-log")
    lazy val combined = org.slf4j.LoggerFactory.getLogger("access-log-combined")
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      if (common.isTraceEnabled || combined.isTraceEnabled) {
        service(request) flatMap { case resp =>
          val reqHeaders = request.headers()
          val remoteHost = request.remoteHost
          val identd = "-"
          val user = "-"
          val requestEndTime = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss Z").format(new Date())
          val reqResource = s"${request.method} ${request.uri} ${request.getProtocolVersion()}"
          val statusCode = resp.statusCode
          val responseBytes = asOpt(resp.headers().get("Content-Length")).getOrElse("-")

          if (common.isTraceEnabled) {
            common.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s""")
          }
          if (combined.isTraceEnabled) {
            val referer = asOpt(reqHeaders.get("Referer")).getOrElse("-")
            val userAgent = asOpt(reqHeaders.get("User-Agent")).getOrElse("-")
            combined.trace(f"""$remoteHost%s $identd%s $user%s [$requestEndTime%s] "$reqResource%s" $statusCode%d $responseBytes%s "$referer%s" "$userAgent%s"""")
          }
          resp.toFuture
        }
      } else {
        service(request)
      }
    }

    def asOpt(value: String) = if (value == null) None else Some(value)
  }


}
