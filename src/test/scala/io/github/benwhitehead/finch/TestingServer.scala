package io.github.benwhitehead.finch

import java.net.InetSocketAddress

import io.finch._

/**
  * @author Ben Whitehead
  */
object TestingServer extends FinchServer {

  val echo: Endpoint[String] = get("echo" / string) { (phrase: String) =>
    Ok(phrase)
  }

  val getJsonBlob: Endpoint[Map[String, String]] = get("json") {
    val data = (1 to 100).map { case i => s"$i" -> String.valueOf(i) }.toMap
    Ok(data)
  }
  val postJsonBlob: Endpoint[String] = post("json" ? binaryBody) { (body: Array[Byte]) =>
    logger.info("body = {}", new String(body))
    Accepted("")
  }

  override lazy val defaultHttpPort = 19990
  override lazy val config = Config(httpInterface = Some(new InetSocketAddress(7070)))
  override lazy val serverName = "test-server"

  def service = {
    import io.finch.EncodeResponse._
    (echo :+: getJsonBlob :+: postJsonBlob).toService
  }
}
