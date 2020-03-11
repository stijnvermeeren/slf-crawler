import java.io.File
import java.net.URL

import org.apache.commons.text.StringEscapeUtils

import scala.util.Random
import scala.io.Source
import sys.process._

object SlfWebsite {
  val baseDomain: String = "https://www.slf.ch"

  def downloadImage(url: URL, target: String): File = {
    println(s"Downloading $url")
    val file = new File(target)
    url #> file !!

    file
  }

  /**
    * Crawl the main page at https://www.slf.ch/de/lawinenbulletin-und-schneesituation/archiv.html
    *
    * Returns a URL to the year archive page, and the year itself as an integer
    */
  def yearArchive(year: Int): Option[URL] = {
    val substrings = Set(
      "de/lawinenbulletin-und-schneesituation/archiv.html",
      s"tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year"
    )

    crawlForHrefWithPrefix(new URL("https://www.slf.ch/de/lawinenbulletin-und-schneesituation/archiv.html"), substrings)
  }

  def categoryArchive(yearArchiveUrl: URL, year: Int, slfKey: String): Option[URL] = {
    val substrings = Set(
      "de/lawinenbulletin-und-schneesituation/archiv.html",
      s"tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F$slfKey"
    )

    crawlForHrefWithPrefix(yearArchiveUrl, substrings)
  }

  def languageArchive(categoryArchiveUrl: URL, year: Int, slfKey: String, language: String): Option[URL] = {
    val substrings = Set(
      "de/lawinenbulletin-und-schneesituation/archiv.html",
      s"tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F$slfKey%2F$language"
    )

    crawlForHrefWithPrefix(categoryArchiveUrl, substrings)
  }

  def fileTypeArchive(categoryArchiveUrl: URL, year: Int, slfKey: String, language: String, fileType: String): Option[URL] = {
    val substrings = Set(
      "de/lawinenbulletin-und-schneesituation/archiv.html",
      s"tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F$slfKey%2F$language%2F$fileType"
    )

    crawlForHrefWithPrefix(categoryArchiveUrl, substrings)
  }

  protected def crawlForHrefWithPrefix(url: URL, substrings: Set[String]): Option[URL] = {
    val source = loadSource(url)

    val hrefRegex = """href="([^"]+)"""".r
    val hrefMatches = hrefRegex.findAllMatchIn(source)

    hrefMatches.toList map {
      case hrefRegex(urlString) =>
        StringEscapeUtils.unescapeHtml4(urlString)
    } find { relativeUrl =>
      substrings.forall(relativeUrl.contains)
    } map { relativeUrl =>
      val cacheBust = Random.nextInt()
      new URL(s"$baseDomain$relativeUrl")
    }
  }

  def crawlImages(url: URL, year: Int, category: Category): Seq[Image] = {
    val source = loadSource(url)

    val hrefRegex = """href="([^"]+)"""".r
    val hrefMatches = hrefRegex.findAllMatchIn(source)

    hrefMatches.toList map {
      case hrefRegex(urlString) =>
        StringEscapeUtils.unescapeHtml4(urlString)
    } flatMap { urlString =>
      category.matchHref(year)(urlString)
    }
  }

  protected def loadSource(url: URL): String = {
    println()
    println(s"Loading $url")
    Source.fromURL(url).getLines() mkString "\n"
  }
}
