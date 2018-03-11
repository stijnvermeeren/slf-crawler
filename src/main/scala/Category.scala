import java.net.URL

import scala.util.matching.Regex

trait Category {
  val key: String

  def slfKeys(year: Int): Seq[String]

  override def toString: String = key

  protected val regex: Regex = """.*\/([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{4})?_(\w+)_(\w+)_c\.(gif|png)""".r

  def lang(year: Int): String = {
    if (year <= 2008) "de" else "en"
  }

  def fileTypes(year: Int): Seq[String]

  def matchHref(year: Int)(href: String): Option[Image] = {
    href match {
      case regex(dateYear, month, day, timeOfDay, slfKey, language, extension) if slfKeys(year).contains(slfKey) && lang(year) == language =>
        val hourOfDay = if (Option(timeOfDay).nonEmpty) Some(Integer.parseInt(timeOfDay) / 100) else None
        Some(Image(
          category = this,
          dateString = s"$dateYear-$month-$day",
          hourOfDay = hourOfDay,
          extension = extension,
          url = new URL(s"${SlfWebsite.baseDomain}/$href"))
        )
      case _ =>
        None
    }
  }
}

object RiskLevels extends Category {
  val key = "risk"
  val startYear = 2001

  def slfKeys(year: Int): Seq[String] = {
    if (year <= 2008) Seq("nbk")
    else if (year <= 2012) Seq("gk")
    else Seq("bki")
  }

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }
}

trait FreshSnow extends Category {
  val startYear = 2002

  def fileTypes(year: Int): Seq[String] = {
    val gif = if (year <= 2014) Seq("gif") else Seq.empty
    val png = if (year >= 2014) Seq("png") else Seq.empty
    gif ++ png
  }
}

object FreshSnow1Day extends FreshSnow {
  def slfKeys(year: Int): Seq[String] = Seq("hn1")
  val key = "1day"
}

object FreshSnow3Days extends FreshSnow {
  def slfKeys(year: Int): Seq[String] = Seq("hn3")
  val key = "3days"
}


object RelativeDepth extends Category {
  val startYear = 2003
  val key = "relative"
  def slfKeys(year: Int): Seq[String] = Seq("hsrel")

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }
}

object Depth extends Category {
  val startYear = 2005
  val key = "depth"

  def slfKeys(year: Int): Seq[String] = {
    Seq("hstop")
  }

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }
}

object DepthAt2000m extends Category {
  val key = "at2000m"
  val startYear = 2002
  def slfKeys(year: Int): Seq[String] = {
    if (year <= 2010) Seq("hsr2000", "hsr2500") else Seq("hsr2000")
  }

  def fileTypes(year: Int): Seq[String] = {
    Seq("gif")
  }
}
