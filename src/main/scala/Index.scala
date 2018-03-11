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

  val s3Image: S3Image = S3Image(category.key, fileName, extension)
}

case class S3Image(
  category: String,
  fileName: String,
  extension: String
) {

  val s3Key: String = s"$category/$fileName.$extension"
  val s3KeyOptimised: String = s"$category/optimised/$fileName.png"
  val s3KeyThumb: String = s"$category/thumb/$fileName.png"
}

object S3Image {
  def fromIndex(indexKey: String): S3Image = {
    val List(category, fileName, extension) = indexKey.split('/').flatMap(_.split('.')).toList
    S3Image(category, fileName, extension)
  }
}
