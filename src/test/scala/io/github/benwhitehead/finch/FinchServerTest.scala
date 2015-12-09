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

import java.io.{BufferedReader, File, FileReader}
import java.util.concurrent.{Future => JFuture, Callable, Executors}

import com.twitter.conversions.time.intToTimeableNumber
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Http, RequestBuilder}
import com.twitter.util._
import io.finch._
import org.scalatest.{BeforeAndAfterEach, FreeSpec}

import scala.collection.mutable.ArrayBuffer

/**
 * @author Ben Whitehead
 */
class FinchServerTest extends FreeSpec with BeforeAndAfterEach {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)
  val echos = ArrayBuffer[String]()
  val echo: Endpoint[String] = get("echo" / string) { (phrase: String) =>
    echos += phrase
    Ok(phrase)
  }
  class TestServer extends FinchServer {
    import io.finch.EncodeResponse.encodeString
    lazy val pidFile = File.createTempFile("testServer", ".pid", new File(System.getProperty("java.io.tmpdir")))
    pidFile.deleteOnExit()
    override lazy val defaultHttpPort = 0
    override lazy val config = Config(port = 0, pidPath = pidFile.getAbsolutePath)
    override lazy val serverName = "test-server"
    def service = echo.toService
  }

  class TestClient(hostPort: String) {
    lazy val client =
      ClientBuilder().
        codec(Http()).
        hosts(hostPort).
        tcpConnectTimeout(1.second).
        requestTimeout(10.seconds).
        hostConnectionLimit(1).
        retries(10).
        build
    def close() = client.close()

    def get(uri: String): Future[String] = {
      client(RequestBuilder().url(s"http://$hostPort/$uri".replaceAll("(?<!:)//", "/")).buildGet) flatMap {
        case response =>
          val buf = response.content
          val out = Array.ofDim[Byte](buf.length)
          buf.write(out, 0)
          Future.value(new String(out, "UTF-8"))
      }
    }
  }

  var server: TestServer = null
  var serverThread: JFuture[Unit] = null
  var client: TestClient = null

  override protected def beforeEach() = {
    server = new TestServer
    serverThread = Executors.newSingleThreadExecutor().submit(
      new Callable[Unit]() {
        override def call(): Unit = server.main(Array())
      }
    )
    server.awaitServerStartup()
    client = new TestClient(s"localhost:${server.serverPort}")
  }
  override protected def afterEach()= {
    server.close()
    serverThread = null
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
      assert(echos.contains("test"))
    }

    "handle 5 requests" in {
      val fs = Future.collect(
        (1 to 5) map { i => client.get(s"/echo/test$i")}
      )
      val resp = Await.result(fs)
      assert(resp.size === 5)
      assert(resp.toSet === Set("test1", "test2", "test3", "test4", "test5"))
      assert(echos.contains("test1"))
      assert(echos.contains("test2"))
      assert(echos.contains("test3"))
      assert(echos.contains("test4"))
      assert(echos.contains("test5"))
    }
  }
}
