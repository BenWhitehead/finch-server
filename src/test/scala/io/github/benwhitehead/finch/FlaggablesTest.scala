package io.github.benwhitehead.finch

import java.net.InetSocketAddress

import com.twitter.app.{Flaggable, Flags}
import com.twitter.app.Flaggable._
import io.github.benwhitehead.finch.Flaggables._
import org.scalatest.FreeSpec

class FlaggablesTest extends FreeSpec {

  "flagOfOption should" - {
    "parse a non-empty string as a Some" in {
      doTest("glavin", Some("glavin"))
    }
    "parse a non-empty string and remove leading and trailing space as a Some" in {
      doTest("   glavin   ", Some("glavin"))
    }
    "parse an empty string as a None" in {
      doTest[String]("", None)
    }
    "parse a string only containing spaces as a None" in {
      doTest[String]("   ", None)
    }

    "parse a non-empty string and transform to the expected type (int)" in {
      doTest("1", Some(1))
    }

    "parse a non-empty string and transform to the expected type (inet address, only port)" in {
      doTest(":1234", Some(new InetSocketAddress(1234)))
    }

    "parse a non-empty string and transform to the expected type (inet address, addr and port)" in {
      doTest("0.0.0.0:1234", Some(new InetSocketAddress("0.0.0.0", 1234)))
    }

    "parse an empty string with and expected transformation" in {
      doTest[InetSocketAddress]("", None)
    }
  }

  private[this] def doTest[T](in: String, expectedResult: Option[T])(implicit flaggable: Flaggable[T]): Unit = {
    val flags = new Flags("")
    val f = flags.apply[Option[T]]("test", None, "help")
    f.parse(in)

    val act = f()

    assert(act === expectedResult)
  }

}
