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

import java.io.{File, FileNotFoundException, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net.{InetSocketAddress, SocketAddress}

import com.twitter.app.App
import com.twitter.conversions.storage.intToStorageUnitableWholeNumber
import com.twitter.finagle._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.netty3.Netty3ListenerTLSConfig
import com.twitter.finagle.param.Label
import com.twitter.finagle.ssl.Ssl
import com.twitter.server.Lifecycle.Warmup
import com.twitter.server.{Admin, AdminHttpServer, Lifecycle, Stats}
import com.twitter.util.{Await, CountDownLatch}
import io.github.benwhitehead.finch.filters._

trait FinchServer extends App
  with SLF4JLogging
  with AdminHttpServer
  with Admin
  with Lifecycle
  with Warmup
  with Stats {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)

  lazy val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head

  implicit val stats = statsReceiver

  case class Config(
    httpInterface: Option[InetSocketAddress] = Some(new InetSocketAddress("0.0.0.0", 7070)),
    pidPath: String = "",
    httpsInterface: Option[InetSocketAddress] = None,
    certificatePath: String = "",
    keyPath: String = "",
    maxRequestSize: Int = 5
  )
  object DefaultConfig extends Config(
    httpInterface(),
    pidFile(),
    httpsInterface(),
    certificatePath(),
    keyPath(),
    maxRequestSize()
  )

  def serverName: String = "finch"
  def service: Service[Request, Response]
  lazy val config: Config = DefaultConfig

  @volatile private var server: Option[ListeningServer] = None
  @volatile private var tlsServer: Option[ListeningServer] = None
  private val cdl = new CountDownLatch(1)

  def writePidFile() {
    val pidFile = new File(config.pidPath)
    val pidFileStream = new FileOutputStream(pidFile)
    pidFileStream.write(pid.getBytes)
    pidFileStream.close()
  }

  def removePidFile() {
    val pidFile = new File(config.pidPath)
    pidFile.delete()
  }

  def main(): Unit = {
    if (!config.pidPath.isEmpty) {
      writePidFile()
    }
    logger.info("process " + pid + " started")

    logger.info(s"admin http server started on: ${adminHttpServer.boundAddress}")
    server = startServer()
    server foreach { ls =>
      logger.info(s"http server started on: ${ls.boundAddress}")
      closeOnExit(ls)
    }

    if (!config.certificatePath.isEmpty && !config.keyPath.isEmpty) {
      verifyFileReadable(config.certificatePath, "SSL Certificate")
      verifyFileReadable(config.keyPath, "SSL Key")
      tlsServer = startTlsServer()
    }

    tlsServer foreach { ls =>
      logger.info(s"https server started on: ${ls.boundAddress}")
      closeOnExit(_)
    }
    cdl.countDown()

    (server, tlsServer) match {
      case (Some(s), Some(ts)) => Await.all(s, ts)
      case (Some(s), None)     => Await.all(s)
      case (None, Some(ts))    => Await.all(ts)
      case (None, None)        => throw new IllegalStateException("No server to wait for startup")
    }
  }

  def awaitServerStartup(): Unit = {
    cdl.await()
  }

  def serverPort: Int = {
    assert(cdl.isZero, "Server not yet started")
    (server map { case s => getPort(s.boundAddress) }).get
  }
  def tlsServerPort: Int = {
    assert(cdl.isZero, "TLS Server not yet started")
    (tlsServer map { case s => getPort(s.boundAddress) }).get
  }

  def startServer(): Option[ListeningServer] = {
    config.httpInterface.map { iface =>
      val name = s"http/$serverName"
      Http.server
        .configured(Label(name))
        .configured(Http.param.MaxRequestSize(config.maxRequestSize.megabytes))
        .serve(iface, getService(s"srv/$name"))
    }
  }

  def startTlsServer(): Option[ListeningServer] = {
    config.httpsInterface.map { iface =>
      val name = s"https/$serverName"
      Http.server
        .configured(Label(name))
        .configured(Http.param.MaxRequestSize(config.maxRequestSize.megabytes))
        .withTls(Netty3ListenerTLSConfig(() => Ssl.server(config.certificatePath, config.keyPath, null, null, null)))
        .serve(iface, getService(s"srv/$name"))
    }
  }

  def getService(serviceName: String): Service[Request, Response] = {
    AccessLog(new StatsFilter(serviceName), accessLog()) andThen
      service
  }

  onExit {
    removePidFile()
  }

  private def getPort(s: SocketAddress): Int = {
    s match {
      case inet: InetSocketAddress => inet.getPort
      case _ => throw new RuntimeException(s"Unsupported SocketAddress type: ${s.getClass.getCanonicalName}")
    }
  }

  private def verifyFileReadable(path: String, description: String): Unit = {
    val file = new File(path)
    if (file.isFile && !file.canRead){
      throw new FileNotFoundException(s"$description could not be read: $path")
    }
  }
}
