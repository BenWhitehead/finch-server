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

package io.github.benwhitehead

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import io.finch._
import io.finch.response.{BadRequest, Respond}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

package object finch {

  trait TypedEndpoint[Request <: HttpRequest] extends Endpoint[Request, HttpResponse]
  trait HttpEndpoint extends TypedEndpoint[HttpRequest]

  class BadRequest            extends Exception // 400
  class Unauthorized          extends Exception // 401
  class PaymentRequired       extends Exception // 402
  class Forbidden             extends Exception // 403
  class NotFound              extends Exception // 404
  class MethodNotAllowed      extends Exception // 405
  class NotAcceptable         extends Exception // 406
  class RequestTimeOut        extends Exception // 408
  class Conflict              extends Exception // 409
  class PreconditionFailed    extends Exception // 412
  class TooManyRequests       extends Exception // 429
  class InternalServerError   extends Exception // 500
  class NotImplemented        extends Exception // 501
  class BadGateway            extends Exception // 502
  class ServiceUnavailable    extends Exception // 503

  case class RespondWithException(response: Response) extends Exception
  class AcceptJsonOnlyException extends RespondWithException(
    BadRequest.withHeaders("Accept" -> "application/json; charset=utf-8")()
  )

  object Accepted extends Respond(Status.Accepted)
  object BadGateway extends Respond(Status.BadGateway)
  object ServiceUnavailable extends Respond(Status.ServiceUnavailable)

  // this is a class rather than an object so that it can be type
  // parametrized
  case class OptionResponse[T]() extends Service[Option[T], T] {
    def apply(request: Option[T]): Future[T] = {
      request match {
        case Some(value) => value.toFuture
        case None => throw new NotFound
      }
    }
  }

  object JacksonResponseSerializer extends Service[Any, HttpResponse] {
    override def apply(request: Any): Future[HttpResponse] = {
      val rep = Response(HttpResponseStatus.OK)
      rep.setContentTypeJson()
      rep.setContentString(JacksonWrapper.serialize(request))
      rep.toFuture
    }
  }

}
