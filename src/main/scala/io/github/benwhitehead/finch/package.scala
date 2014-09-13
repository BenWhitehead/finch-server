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

import com.twitter.app.{App => TApp}
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.server.Stats
import com.twitter.util.Future
import io.finch._
import io.finch.json.JsonObject
import io.finch.request.{ParamNotFound, ValidationFailed}
import io.finch.response._
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import java.text.SimpleDateFormat
import java.util.Date

package object finch {

  abstract class SimpleEndpoint extends Endpoint[HttpRequest, HttpResponse]

  class NotFound extends Exception
  class BadRequest extends Exception
  class Forbidden extends Exception

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
