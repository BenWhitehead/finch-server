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
import com.twitter.finagle.httpx.Response
import com.twitter.util.Future
import io.finch._
import io.finch.response.BadRequest

package object finch {

  trait TypedEndpoint[Request] extends Endpoint[Request, HttpResponse]
  trait HttpEndpoint extends TypedEndpoint[HttpRequest]

  sealed class HttpException(val status: Int) extends Exception
  class BadRequest            extends HttpException(400)
  class Unauthorized          extends HttpException(401)
  class PaymentRequired       extends HttpException(402)
  class Forbidden             extends HttpException(403)
  class NotFound              extends HttpException(404)
  class MethodNotAllowed      extends HttpException(405)
  class NotAcceptable         extends HttpException(406)
  class RequestTimeOut        extends HttpException(408)
  class Conflict              extends HttpException(409)
  class PreconditionFailed    extends HttpException(412)
  class TooManyRequests       extends HttpException(429)
  class InternalServerError   extends HttpException(500)
  class NotImplemented        extends HttpException(501)
  class BadGateway            extends HttpException(502)
  class ServiceUnavailable    extends HttpException(503)

  case class RespondWithException(response: Response) extends Exception
  class AcceptJsonOnlyException extends RespondWithException(
    BadRequest.withHeaders("Accept" -> "application/json; charset=utf-8")()
  )

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

}
