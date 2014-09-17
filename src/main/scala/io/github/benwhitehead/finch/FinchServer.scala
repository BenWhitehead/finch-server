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

import com.twitter.app.App

/**
 * @author Ben Whitehead
 */
trait FinchServer extends App {

  lazy val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head

  case class Config(port: Int = 7070, pidPath: String = "", adminPort: Int = 9990)

  def serverName: String = "finch"
  lazy val config: Config = new Config(
    httpPort(),
    pidFile(),
    adminHttpPort()
  )

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
    println("process " + pid + " started")
  }
}
