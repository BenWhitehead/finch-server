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

import com.twitter.app.App
import com.twitter.conversions.storage.intToStorageUnitableWholeNumber
import com.twitter.finagle._
import com.twitter.finagle.httpx.service.NotFoundService
import com.twitter.finagle.netty3.Netty3ListenerTLSConfig
import com.twitter.finagle.ssl.Ssl
import com.twitter.server.Lifecycle.Warmup
import com.twitter.server.{Admin, AdminHttpServer, Lifecycle, Stats}
import com.twitter.util.Await
import io.finch._
import io.github.benwhitehead.finch.filters._

import java.io.{File, FileNotFoundException, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net.{InetSocketAddress, SocketAddress}

trait FinchServer[Request <: HttpRequest] extends App
  with SLF4JLogging
  with AdminHttpServer
  with Admin
  with Lifecycle
  with Warmup
  with Stats {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)

  lazy val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head

  case class Config(
    port: Int = 7070,
    pidPath: String = "",
    httpsPort: Int = 7443,
    certificatePath: String = "",
    keyPath: String = "",
    maxRequestSize: Int = 5/*,
    decompressionEnabled: Boolean = false,
    compressionLevel: Int = 6*/
  )
  object DefaultConfig extends Config(
    httpPort(),
    pidFile(),
    httpsPort(),
    certificatePath(),
    keyPath(),
    maxRequestSize()/*,
    decompressionEnabled(),
    compressionLevel()*/
  )

  def serverName: String = "finch"
  def endpoint: Endpoint[Request, HttpResponse]
  def filter: Filter[HttpRequest, HttpResponse, Request, HttpResponse]
  lazy val config: Config = DefaultConfig

  @volatile private var server: Option[ListeningServer] = None
  @volatile private var tlsServer: Option[ListeningServer] = None

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
    server = Some(startServer())
    logger.info(s"http server started on: ${(server map {_.boundAddress}).get}")
    server map { closeOnExit(_) }

    if (!config.certificatePath.isEmpty && !config.keyPath.isEmpty) {
      verifyFileReadable(config.certificatePath, "SSL Certificate")
      verifyFileReadable(config.keyPath, "SSL Key")
      tlsServer = Some(startTlsServer())
      logger.info(s"https server started on: ${(tlsServer map {_.boundAddress}).get}")
    }

    tlsServer map { closeOnExit(_) }

    (server, tlsServer) match {
      case (Some(s), Some(ts)) => Await.all(s, ts)
      case (Some(s), None)     => Await.all(s)
      case (None, Some(ts))    => Await.all(ts)
      case (None, None)        => throw new IllegalStateException("No server to wait for startup")
    }
  }

  def serverPort: Int = (server map { case s => getPort(s.boundAddress) }).get
  def tlsServerPort: Int = (tlsServer map { case s => getPort(s.boundAddress) }).get

  def startServer(): ListeningServer = {
    val name = s"http/$serverName"
    Httpx.server
      .configured(param.Label(name))
      .configured(Httpx.param.MaxRequestSize(config.maxRequestSize.megabytes))
      // TODO: Figure out how to add back compression support
      .serve(new InetSocketAddress(config.port), getService(s"srv/$name"))
  }

  def startTlsServer(): ListeningServer = {
    val name = s"https/$serverName"
    Httpx.server
      .configured(param.Label(name))
      .configured(Httpx.param.MaxRequestSize(config.maxRequestSize.megabytes))
      .withTls(Netty3ListenerTLSConfig(() => Ssl.server(config.certificatePath, config.keyPath, null, null, null)))
      // TODO: Figure out how to add back compression support
      .serve(new InetSocketAddress(config.httpsPort), getService(s"srv/$name"))
  }

  def getService(serviceName: String) = {
    new StatsFilter(serviceName) andThen
      AccessLog andThen
      errorHandler andThen
      filter andThen
      (endpoint orElse NotFound).toService
  }

  def errorHandler: Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] = HandleExceptions

  onExit {
    removePidFile()
  }

  val NotFound = new Endpoint[Request, HttpResponse] {
    lazy val underlying = new NotFoundService[Request]
    def route = { case _ => underlying }
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
      throw new FileNotFoundException(s"$description could not be read: ${config.keyPath}")
    }
  }
}

trait SimpleFinchServer extends FinchServer[HttpRequest] {
  override def filter = Filter.identity
}
