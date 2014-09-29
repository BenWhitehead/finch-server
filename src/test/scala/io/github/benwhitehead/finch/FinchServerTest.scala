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

import java.io.{FileReader, BufferedReader, File}

import com.twitter.conversions.time.longToTimeableNumber
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.path.{->, /, Root}
import com.twitter.finagle.http.{Http, Method, RequestBuilder}
import com.twitter.util._
import io.finch._
import io.finch.response._
import org.jboss.netty.util.CharsetUtil
import org.scalatest.{BeforeAndAfterEach, FreeSpec}

/**
 * @author Ben Whitehead
 */
class FinchServerTest extends FreeSpec with BeforeAndAfterEach {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)

  class Echo extends HttpEndpoint {
    var echos: List[String] = Nil

    def service(echo: String) = new Service[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest): Future[HttpResponse] = {
        echos = echos :+ echo
        Ok(echo).toFuture
      }
    }
    def route = {
      case Method.Get -> Root / "echo" / echo => service(echo)
    }
  }

  class TestServer extends SimpleFinchServer {
    lazy val pidFile = File.createTempFile("testServer", ".pid", new File(System.getProperty("java.io.tmpdir")))
    pidFile.deleteOnExit()
    override lazy val config = Config(port = 0, pidPath = pidFile.getAbsolutePath, adminPort = 0)
    override lazy val serverName = "test-server"
    lazy val echos = new Echo
    def endpoint = {
       echos
    }
  }

  class TestClient(hostPort: String) {
    lazy val client =
      ClientBuilder().
        codec(Http()).
        hosts(hostPort).
        tcpConnectTimeout(1.second).
        requestTimeout(10.seconds).
        hostConnectionLimit(1).
        build
    def close() = client.close()

    def get(uri: String): Future[String] = {
      client(RequestBuilder().url(s"http://$hostPort/$uri".replaceAll("(?<!:)//", "/")).buildGet) flatMap {
        case response =>
          Future.value(response.getContent.toString(CharsetUtil.UTF_8))
      }
    }
  }

  var server: TestServer = null
  var serverThread: Thread = null
  var client: TestClient = null

  override protected def beforeEach() = {
    server = new TestServer
    serverThread = new Thread {
      override def run() = server.main(Array())
    }
    serverThread.start()  // TODO: Figure out a better way to start the server in another thread or background it
    Thread.sleep(2500) // TODO: Figure out how to "Await" on the server
    client = new TestClient(s"localhost:${server.serverPort}")
  }
  override protected def afterEach()= {
    server.close()
    serverThread.stop()
    client.close()
  }

  "start server" - {
    "write pid" in {
      val reader = new BufferedReader(new FileReader(server.pidFile))
      val pid = reader.readLine().toInt
      logger.info(s"pid = $pid")
      assert(pid > 1)
    }

    "handle request" in {
      val resp = Await.result(client.get("/echo/test"))
      assert(resp === "test")
      assert(server.echos.echos.contains("test"))
    }

    "handle 5 requests" in {
      val fs = Future.collect(
        (1 to 5) map { i => client.get(s"/echo/test$i")}
      )
      val resp = Await.result(fs)
      assert(resp.size === 5)
      assert(resp.toSet === Set("test1", "test2", "test3", "test4", "test5"))
      assert(server.echos.echos.contains("test1"))
      assert(server.echos.echos.contains("test2"))
      assert(server.echos.echos.contains("test3"))
      assert(server.echos.echos.contains("test4"))
      assert(server.echos.echos.contains("test5"))
    }
  }
}
