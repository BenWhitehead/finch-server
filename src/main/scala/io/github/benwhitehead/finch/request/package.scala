package io.github.benwhitehead.finch

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.finch._
import io.finch.request.RequestReader

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

}
