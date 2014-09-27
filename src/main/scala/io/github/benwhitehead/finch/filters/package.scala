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
import io.finch._
import io.finch.json.JsonObject
import io.finch.request.{ParamNotFound, ValidationFailed}
import io.finch.response._

/**
 * @author Ben Whitehead
 */
package object filters {

  object HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
    lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      service(request) handle {
        case e: ValidationFailed => BadRequest(JsonObject("message" -> e.getMessage))
        case e: ParamNotFound => BadRequest(JsonObject("message" -> e.getMessage))
        case e: BadRequest => BadRequest()
        case e: Unauthorized => Unauthorized()
        case e: PaymentRequired => PaymentRequired()
        case e: Forbidden => Forbidden()
        case e: NotFound => NotFound()
        case e: MethodNotAllowed => MethodNotAllowed()
        case e: NotAcceptable => NotAcceptable()
        case e: RequestTimeOut => RequestTimeOut()
        case e: Conflict => Conflict()
        case e: PreconditionFailed => PreconditionFailed()
        case e: TooManyRequests => TooManyRequests()
        case e: NotImplemented => NotImplemented()
        case e: BadGateway => BadGateway()
        case e: ServiceUnavailable => ServiceUnavailable()
        case t: Throwable => logger.error("", t); InternalServerError()
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
