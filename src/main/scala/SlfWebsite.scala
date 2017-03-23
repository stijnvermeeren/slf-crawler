import java.io.File
import java.net.URL

import scala.io.Source
import sys.process._

object SlfWebsite {
  val baseDomain: String = "http://www.slf.ch"

  def downloadImage(url: URL, target: String): File = {
    println(s"Downloading $url")
    val file = new File(target)
    url #> file !!

    file
  }

  def listImages(year: Int, category: Category): Seq[Image] = {
    val results: Seq[Image] = category.overview(year) flatMap { overview =>
      val url = new URL(s"$baseDomain/schneeinfo/Archiv/lwdarchiv/$overview")

      val hrefRegex = """href\=\"([^"]+)\"""".r

      val source = lines(url) mkString "\n"

      hrefRegex.findAllMatchIn(source) map {
        _.group(1)
      } flatMap {
        category.matchHref(year)
      }
    }

    results.distinct
  }

  protected def lines(url: URL): Iterator[String] = {
    println(s"Loading $url")
    Source.fromURL(url).getLines()
  }

  /* def imageUrl(image: Image): URL = {
    new URL(s"http://www.slf.ch/schneeinfo/Archiv/lwdarchiv/$year/$category/en/gif/$fileName")
  } */
}
