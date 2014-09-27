package io.github.benwhitehead.finch

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.path.{->, /, Root}
import com.twitter.util.Future
import io.finch._
import io.finch.response._

/**
 * @author Ben Whitehead
 */

object Echo extends HttpEndpoint {
  def service(echo: String) = new Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(echo).toFuture
    }
  }
  def route = {
    case Method.Get -> Root / "echo" / echo => service(echo)
  }
}

object TestingServer extends SimpleHttpFinchServer {
  override lazy val config = Config(port = 17070, adminPort = 19990)
  override lazy val serverName = "test-server"
  def endpoint = {
    Echo
  }
}
