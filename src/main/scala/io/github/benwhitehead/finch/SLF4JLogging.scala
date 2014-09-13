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

import java.util.logging.{LogManager, Level}

import org.slf4j.bridge.SLF4JBridgeHandler

/**
 * @author Ben Whitehead
 */
object SLF4JLogging {

  def install(): Unit = {
    // Turn off Java util logging so that slf4j can configure it
    LogManager.getLogManager.getLogger("").getHandlers.toList.map { l =>
      l.setLevel(Level.OFF)
    }
    SLF4JBridgeHandler.install()
  }

  def uninstall(): Unit = {
    SLF4JBridgeHandler.uninstall()
  }

  def apply(f: => Unit): Unit = {
    install()
    try {
      f
    }
    finally {
      uninstall()
    }
  }
}
