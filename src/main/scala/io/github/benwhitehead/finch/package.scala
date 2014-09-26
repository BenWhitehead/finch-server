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
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import io.finch._
import io.finch.request.RequestReader
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.util.CharsetUtil

import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

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

  object RequiredStringBody {
    def apply() = new RequestReader[String] {
      def apply(req: HttpRequest): Future[String] = {
        req.contentLength match {
          case Some(length) if length > 0 => req.content.toString(CharsetUtil.UTF_8).toFuture
          case _                          => new BadRequest().toFutureException
        }
      }
    }
  }

  object OptionalStringBody {
    def apply() = new RequestReader[Option[String]] {
      def apply(req: HttpRequest): Future[Option[String]] = {
        req.contentLength match {
          case Some(length) if length > 0 => Some(req.content.toString(CharsetUtil.UTF_8)).toFuture
          case _                          => None.toFuture
        }
      }
    }
  }

  object RequiredJSONObjectBody {
    def apply() = new RequestReader[JSONObject] {
      def apply(req: HttpRequest): Future[JSONObject] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => obj.toFuture
              case Some(arr: JSONArray) => new BadRequest().toFutureException
              case None => new BadRequest().toFutureException
            }
          case _ => new BadRequest().toFutureException
        }
      }
    }
  }

  object RequiredJSONArrayBody {
    def apply() = new RequestReader[JSONArray] {
      def apply(req: HttpRequest): Future[JSONArray] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => new BadRequest().toFutureException
              case Some(arr: JSONArray) => arr.toFuture
              case None => new BadRequest().toFutureException
            }
          case _ => new BadRequest().toFutureException
        }
      }
    }
  }

  object OptionalJSONObjectBody {
    def apply() = new RequestReader[Option[JSONObject]] {
      def apply(req: HttpRequest): Future[Option[JSONObject]] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => Some(obj).toFuture
              case Some(arr: JSONArray) => None.toFuture
              case None => None.toFuture
            }
          case _ => None.toFuture
        }
      }
    }
  }

  object OptionalJSONArrayBody {
    def apply() = new RequestReader[Option[JSONArray]] {
      def apply(req: HttpRequest): Future[Option[JSONArray]] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => None.toFuture
              case Some(arr: JSONArray) => Some(arr).toFuture
              case None => None.toFuture
            }
          case _ => None.toFuture
        }
      }
    }
  }

}
