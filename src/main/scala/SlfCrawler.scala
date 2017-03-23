import java.io.File

import com.typesafe.config.ConfigFactory

object SlfCrawler extends App {
  val config = ConfigFactory.load()
  var accessKeyId = config.getString("s3.accessKeyId")
  var secretAccessKey = config.getString("s3.secretAccessKey")
  var bucket = config.getString("s3.bucket")
  var dataFile = new File(config.getString("local.dataFile"))
  var indexFile = new File(config.getString("local.indexFile"))

  val index = Index.load(indexFile)

  val s3 = new S3(bucket, accessKeyId, secretAccessKey)

  val newImages = crawlYear(s3, index, 2017)

  val newIndex = index ++ newImages.map(_.s3Key)
  Index.save(indexFile)(newIndex)
  JsonData.saveToFile(dataFile, JsonData.fromIndex(newIndex))
  s3.save("data.json", dataFile)

  def crawlCategory(s3: S3, index: Set[String], category: Category, years: Seq[Int]): Seq[Image] = {
    years flatMap { year =>
      SlfWebsite.listImages(year, category) flatMap { image =>
        crawlOne(s3, image, index)
      }
    }
  }

  def crawlYear(s3: S3, index: Set[String], year: Int): Seq[Image] = {
    val categories = Seq(Depth, DepthAt2000m, FreshSnow1Day, FreshSnow3Days, RelativeDepth)

    categories flatMap { category =>
      SlfWebsite.listImages(year, category) flatMap { image =>
        crawlOne(s3, image, index)
      }
    }
  }

  def crawlOne(s3: S3, image: Image, index: Set[String]): Option[Image] = {
    if (!index.contains(image.s3Key)) {
      val tmpFile = SlfWebsite.downloadImage(image.url, "tmp/" + image.name)
      s3.save(image.s3Key, tmpFile)
      tmpFile.delete()

      Some(image)
    } else {
      None
    }
  }
}
