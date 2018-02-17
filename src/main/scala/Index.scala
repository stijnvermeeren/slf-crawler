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
  extension: String,
  url: URL
) {
  val s3Key: String = s"$year/$category/$dateString.$extension"
  val s3KeyOptimised: String = s"$year/$category/optimised/$dateString.png"
  val s3KeyThumb: String = s"$year/$category/thumb/$dateString.png"
}
