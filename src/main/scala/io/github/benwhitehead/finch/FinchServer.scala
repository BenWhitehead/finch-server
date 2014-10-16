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

import java.io.{FileNotFoundException, File, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net.{InetSocketAddress, SocketAddress}

import com.twitter.app.App
import com.twitter.conversions.storage.intToStorageUnitableWholeNumber
import com.twitter.finagle._
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.service.NotFoundService
import com.twitter.finagle.http.{Http, HttpMuxer, RichHttp}
import com.twitter.server.Lifecycle.Warmup
import com.twitter.server.{Admin, Lifecycle, Stats}
import com.twitter.util.Await
import io.finch._
import io.github.benwhitehead.finch.filters._

trait FinchServer[Request <: HttpRequest] extends App
  with SLF4JLogging
  with Admin
  with Lifecycle
  with Warmup
  with Stats {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)

  lazy val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head

  case class Config(
    port: Int = 7070,
    pidPath: String = "",
    adminPort: Int = 9990,
    httpsPort: Int = 7443,
    certificatePath: String = "",
    keyPath: String = "",
    maxRequestSize: Int = 5,
    decompressionEnabled: Boolean = false,
    compressionLevel: Int = 6
  )
  object DefaultConfig extends Config(
    httpPort(),
    pidFile(),
    adminHttpPort(),
    httpsPort(),
    certificatePath(),
    keyPath(),
    maxRequestSize(),
    decompressionEnabled(),
    compressionLevel()
  )

  def serverName: String = "finch"
  def endpoint: Endpoint[Request, HttpResponse]
  def filter: Filter[HttpRequest, HttpResponse, Request, HttpResponse]
  lazy val config: Config = DefaultConfig

  private var server: Option[Server] = None
  private var tlsServer: Option[Server] = None
  private var adminServer: Option[ListeningServer] = None

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

    adminServer = Some(com.twitter.finagle.Http.serve(new InetSocketAddress(config.adminPort), HttpMuxer))
    adminServer map { closeOnExit(_) }
    logger.info(s"admin http server started on: ${(adminServer map {_.boundAddress}).get}")

    server = Some(startServer())
    logger.info(s"http server started on: ${(server map {_.localAddress}).get}")
    server map { closeOnExit(_) }

    if (!config.certificatePath.isEmpty && !config.keyPath.isEmpty) {
      verifyFileReadable(config.certificatePath, "SSL Certificate")
      verifyFileReadable(config.keyPath, "SSL Key")
      tlsServer = Some(startTlsServer())
      logger.info(s"https server started on: ${(tlsServer map {_.localAddress}).get}")
    }

    tlsServer map { closeOnExit(_) }
    adminServer map { Await.ready(_) }
  }

  def serverPort: Int = (server map { case s => getPort(s.localAddress) }).get
  def tlsServerPort: Int = (tlsServer map { case s => getPort(s.localAddress) }).get
  def adminPort: Int = (adminServer map { case s => getPort(s.boundAddress) }).get

  def startServer(): Server = {
    val name = s"srv/http/$serverName"
    ServerBuilder()
      .codec(getCodec)
      .bindTo(new InetSocketAddress(config.port))
      .name(name)
      .build(getService(name))
  }

  def startTlsServer(): Server = {
    val name = s"srv/https/$serverName"
    ServerBuilder()
      .codec(getCodec)
      .bindTo(new InetSocketAddress(config.httpsPort))
      .tls(config.certificatePath, config.keyPath)
      .name(name)
      .build(getService(name))
  }

  def getCodec: RichHttp[HttpRequest] = {
    val http = Http()
      .maxRequestSize(config.maxRequestSize.megabytes)
    if (config.decompressionEnabled) {
      http
        .decompressionEnabled(config.decompressionEnabled)
        .compressionLevel(config.compressionLevel)
    }

    RichHttp[HttpRequest](http)
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
