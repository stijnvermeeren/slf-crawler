import java.io.{File, PrintWriter}
import java.net.URL

object Index {
  def load(file: File): Set[String] = {
    scala.io.Source.fromFile(file).getLines().toSet
  }

  def save(file: File)(index: Set[String]): Unit = {
    val pw = new PrintWriter(file)
    pw.write(index mkString "\n")
    pw.close()
  }
}

case class Image(
  year: Int,
  category: Category,
  dateString: String,
  hourOfDay: Option[Int],
  extension: String,
  url: URL
) {
  private val fileName: String = {
    hourOfDay match {
      case Some(hour) => s"${dateString}_$hour"
      case None => dateString
    }
  }

  val s3Key: String = s"$year/$category/$fileName.$extension"
  val s3KeyOptimised: String = s"$year/$category/optimised/$fileName.png"
  val s3KeyThumb: String = s"$year/$category/thumb/$fileName.png"
}
