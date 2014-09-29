package io.github.benwhitehead.finch

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.finch._
import io.finch.request.RequestReader
import org.jboss.netty.util.CharsetUtil

import scala.util.parsing.json.{JSONArray, JSON, JSONObject}

package object request {

  class ReaderService[Req <: HttpRequest, T](reader: RequestReader[T]) extends Service[Req, T] {
    final def apply(request: Req): Future[T] = reader(request)
  }
  object ReaderService {
    /* TODO: See if it's possible to improve the type info here so that SimpleReaderService isn't needed */
    def apply[Req <: HttpRequest, T](f: => RequestReader[T]) = new ReaderService[Req, T](f)
  }
  object SimpleReaderService {
    def apply[T](f: => RequestReader[T]) = new ReaderService[HttpRequest, T](f)
  }

  object DelegateService {
    def apply[Req <: HttpRequest, T](f: => T) = new Service[Req, T] {
      def apply(req: Req): Future[T] = {
        f.toFuture
      }
    }
  }

  object RequiredStringBody {
    def apply() = new RequestReader[String] {
      def apply(req: HttpRequest): Future[String] = {
        req.contentLength match {
          case Some(length) if length > 0 => req.content.toString(CharsetUtil.UTF_8).toFuture
          case _                          => new AcceptJsonOnlyException().toFutureException
        }
      }
    }
  }

  /**
   * Use Jackson to attempt to deserialize Request Body into a `T`
   */
  object RequiredJsonBody {
    def apply[T: Manifest]() = new RequestReader[T] {
      def apply(req: HttpRequest): Future[T] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json; charset=utf-8") =>
            JacksonWrapper.deserialize[T](req.content.toString(CharsetUtil.UTF_8)).toFuture
          case _ => new AcceptJsonOnlyException().toFutureException
        }
      }
    }
  }

  object RequiredJSONObjectBody {
    def apply() = new RequestReader[JSONObject] {
      def apply(req: HttpRequest): Future[JSONObject] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json; charset=utf-8") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => obj.toFuture
              case Some(arr: JSONArray) => new AcceptJsonOnlyException().toFutureException
              case None => new AcceptJsonOnlyException().toFutureException
            }
          case _ => new AcceptJsonOnlyException().toFutureException
        }
      }
    }
  }

  object RequiredJSONArrayBody {
    def apply() = new RequestReader[JSONArray] {
      def apply(req: HttpRequest): Future[JSONArray] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json; charset=utf-8") =>
            JSON.parseRaw(req.content.toString(CharsetUtil.UTF_8)) match {
              case Some(obj: JSONObject) => new AcceptJsonOnlyException().toFutureException
              case Some(arr: JSONArray) => arr.toFuture
              case None => new AcceptJsonOnlyException().toFutureException
            }
          case _ => new AcceptJsonOnlyException().toFutureException
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

  object OptionalJSONObjectBody {
    def apply() = new RequestReader[Option[JSONObject]] {
      def apply(req: HttpRequest): Future[Option[JSONObject]] = {
        req.headerMap.get("Content-Type") match {
          case Some("application/json; charset=utf-8") =>
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
          case Some("application/json; charset=utf-8") =>
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
