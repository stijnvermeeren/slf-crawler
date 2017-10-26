import java.io.File
import java.net.URL

import org.apache.commons.text.StringEscapeUtils

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
    val prefix = s"de/lawinenbulletin-und-schneesituation/archiv.html?tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year"

    crawlForHrefWithPrefix(new URL("https://www.slf.ch/de/lawinenbulletin-und-schneesituation/archiv.html"), prefix)
  }

  def categoryArchive(yearArchiveUrl: URL, year: Int, category: Category): Option[URL] = {
    val prefix = s"de/lawinenbulletin-und-schneesituation/archiv.html?tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F${category.slfKey}"

    crawlForHrefWithPrefix(yearArchiveUrl, prefix)
  }

  def languageArchive(categoryArchiveUrl: URL, year: Int, category: Category): Option[URL] = {
    val prefix = s"de/lawinenbulletin-und-schneesituation/archiv.html?tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F${category.slfKey}%2F${category.lang(year)}"

    crawlForHrefWithPrefix(categoryArchiveUrl, prefix)
  }

  def fileTypeArchive(categoryArchiveUrl: URL, year: Int, category: Category, fileType: String): Option[URL] = {
    val prefix = s"de/lawinenbulletin-und-schneesituation/archiv.html?tx_wslavalanches_archiv%5Bpath%5D=%2Fuser_upload%2Fimport%2Flwdarchiv%2Fpublic%2F$year%2F${category.slfKey}%2F${category.lang(year)}%2F$fileType"

    crawlForHrefWithPrefix(categoryArchiveUrl, prefix)
  }

  protected def crawlForHrefWithPrefix(url: URL, prefix: String): Option[URL] = {
    val source = loadSource(url)

    val hrefRegex = """href="([^"]+)"""".r
    val hrefMatches = hrefRegex.findAllMatchIn(source)

    hrefMatches.toList map {
      case hrefRegex(urlString) =>
        StringEscapeUtils.unescapeHtml4(urlString)
    } find {
      _.startsWith(prefix)
    } map {
      relativeUrl => new URL(s"$baseDomain/$relativeUrl")
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
