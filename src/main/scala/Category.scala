import java.net.URL

import scala.util.matching.Regex

trait Category {
  def overview(year: Int): Seq[String]

  val key: String
  val slfKey: String

  override def toString: String = key

  protected val regex: Regex = """.*\/([0-9]{4})([0-9]{2})([0-9]{2})(?:[0-9]{4})?_\w+_\w+_c\.(gif|png)""".r

  def lang(year: Int): String = {
    if (year <= 2008) "de" else "en"
  }

  def fileTypes(year: Int): Seq[String]

  def matchHref(year: Int)(href: String): Option[Image] = {
    href match {
      case regex(dateYear, month, day, extension) =>
        Some(Image(year, this, s"$dateYear-$month-$day.$extension", new URL(s"${SlfWebsite.baseDomain}/$href")))
      case _ =>
        None
    }
  }
}

trait FreshSnow extends Category {
  val startYear = 2002

  def fileTypes(year: Int): Seq[String] = {
    val gif = if (year <= 2014) Seq("gif") else Seq.empty
    val png = if (year >= 2014) Seq("png") else Seq.empty
    gif ++ png
  }

  def overview(year: Int): Seq[String] = {
    val gifOverview = if (year <= 2014) Seq(s"$year/$slfKey/${lang(year)}/gif") else Seq.empty
    val pngOverview = if (year >= 2014) Seq(s"$year/$slfKey/${lang(year)}/png") else Seq.empty
    gifOverview ++ pngOverview
  }
}

object FreshSnow1Day extends FreshSnow {
  val slfKey: String = "hn1"
  val key = "1day"
}

object FreshSnow3Days extends FreshSnow {
  val slfKey: String = "hn3"
  val key = "3days"
}


object RelativeDepth extends Category {
  val startYear = 2003
  val key = "relative"
  val slfKey = "hsrel"

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }

  def overview(year: Int): Seq[String] = {
    Seq(s"$year/$slfKey/${lang(year)}/gif")
  }
}

object Depth extends Category {
  val startYear = 2005
  val key = "depth"
  val slfKey = "hstop"

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }

  def overview(year: Int): Seq[String] = {
    Seq(s"$year/$slfKey/${lang(year)}/gif")
  }
}

object DepthAt2000m extends Category {
  val key = "at2000m"
  val startYear = 2002
  val slfKey = "hsr2000"

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }

  def overview(year: Int): Seq[String] = {
    val overview2500 = if (year <= 2010) Seq(s"$year/hsr2500/${lang(year)}/gif") else Seq.empty
    Seq(s"$year/hsr2000/${lang(year)}/gif") ++ overview2500
  }
}
