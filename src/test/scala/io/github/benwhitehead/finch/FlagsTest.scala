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

import java.io.{ByteArrayOutputStream, PrintStream}
import java.net.InetSocketAddress
import java.security.Permission

import io.finch._
import org.scalatest.{BeforeAndAfterEach, FreeSpec}

/**
 * @author Ben Whitehead
 */
class FlagsTest extends FreeSpec with BeforeAndAfterEach {

  val ping: Endpoint[String] = get("ping") { Ok("pong") }

  var origSm: SecurityManager = null

  var server: FinchServer = null
  override protected def beforeEach() = {
    origSm = System.getSecurityManager
    System.setSecurityManager(new SystemExitTrap)

    server = new FinchServer {
      def service = ping.toService
      override def main() = {}
    }
  }

  override protected def afterEach() = {
    System.setSecurityManager(origSm)
  }

  "flags" - {
    "defaults" in {
      server.main(Array())
      val config = server.config
      assert(config.httpInterface === Some(new InetSocketAddress("0.0.0.0", 7070)))
      assert(server.adminPort() === new InetSocketAddress(9990))
      assert(config.pidPath === "")
    }

    "set" in {
      server.main(Array(
        "-io.github.benwhitehead.finch.httpInterface=:1234",
        "-admin.port=:4321",
        "-io.github.benwhitehead.finch.pidFile=/tmp/server.pid"
      ))
      val config = server.config
      assert(config.httpInterface === Some(new InetSocketAddress("0.0.0.0", 1234)))
      assert(server.adminPort() === new InetSocketAddress(4321))
      assert(config.pidPath === "/tmp/server.pid")
    }

    "allow for https only server" in {
      server.main(Array(
        "-io.github.benwhitehead.finch.httpInterface=",
        "-io.github.benwhitehead.finch.httpsInterface=:12345",
        "-admin.port=:4321",
        "-io.github.benwhitehead.finch.pidFile=/tmp/server.pid"
      ))
      val config = server.config
      assert(config.httpInterface === None)
      assert(config.httpsInterface === Some(new InetSocketAddress("0.0.0.0", 12345)))
      assert(server.adminPort() === new InetSocketAddress(4321))
      assert(config.pidPath === "/tmp/server.pid")
    }

    "-help works" in {
      val stderr = System.err
      val baos = new ByteArrayOutputStream()
      System.setErr(new PrintStream(baos))
      try {
        server.main(Array("-help"))
      }
      catch {
        case e: SecurityException =>
          assert(e.getMessage === "System.exit(1) trapped")
          val string = baos.toString
          assert(string.contains("-admin.port"))
          assert(string.contains("-io.github.benwhitehead.finch.httpInterface=':7070'"))
          assert(string.contains("-io.github.benwhitehead.finch.httpsInterface=''"))
          assert(string.contains("-io.github.benwhitehead.finch.pidFile"))
      } finally {
        System.setErr(stderr)
      }
    }
  }


  class SystemExitTrap extends SecurityManager {
    override def checkExit(status: Int): Unit = throw new SecurityException(s"System.exit($status) trapped")
    override def checkPermission(perm: Permission): Unit = {}
    override def checkPermission(perm: Permission, context: scala.Any): Unit = {}
  }
}
