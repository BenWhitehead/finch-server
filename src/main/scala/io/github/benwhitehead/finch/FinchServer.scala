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

import java.io.{File, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net.{SocketAddress, InetSocketAddress}

import com.twitter.app.App
import com.twitter.finagle.{ListeningServer, HttpServer}
import com.twitter.finagle.builder.{ServerBuilder, Server}
import com.twitter.finagle.http.{Http, HttpMuxer, RichHttp}
import com.twitter.util.Await
import io.finch._
import io.github.benwhitehead.finch.filters._

/**
 * @author Ben Whitehead
 */
trait FinchServer extends App {
  lazy val logger = org.slf4j.LoggerFactory.getLogger(getClass.getName)

  lazy val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head

  case class Config(port: Int = 7070, pidPath: String = "", adminPort: Int = 9990)

  def serverName: String = "finch"
  def endpoint: Endpoint[HttpRequest, HttpResponse]
  lazy val config: Config = new Config

  private var server: Option[Server] = None
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

  def main() = {
    if (!config.pidPath.isEmpty) {
      writePidFile()
    }
    logger.info("process " + pid + " started")

    adminServer = Some(HttpServer.serve(new InetSocketAddress(config.adminPort), HttpMuxer))
    adminServer map { closeOnExit(_) }
    logger.info(s"admin http server started on: ${(adminServer map {_.boundAddress}).get}")

    server = Some(startServer())
    logger.info(s"http server started on: ${(server map {_.localAddress}).get}")
    server map { closeOnExit(_) }

    adminServer map { Await.ready(_) }
  }

  def serverPort: Int = (server map { case s => getPort(s.localAddress) }).get
  def adminPort: Int = (adminServer map { case s => getPort(s.boundAddress) }).get

  def startServer(): Server = {
    ServerBuilder()
      .codec(RichHttp[HttpRequest](Http()))
      .bindTo(new InetSocketAddress(config.port))
      .name(s"srv/http/$serverName")
      .build(StatsFilter andThen AccessLog andThen HandleErrors andThen endpoint.toService)
  }

  init {
    SLF4JLogging.install()
  }

  onExit {
    SLF4JLogging.uninstall()
    removePidFile()
  }

  private def getPort(s: SocketAddress): Int = {
    s match {
      case inet: InetSocketAddress => inet.getPort
      case _ => throw new RuntimeException(s"Unsupported SocketAddress type: ${s.getClass.getCanonicalName}")
    }
  }
}
