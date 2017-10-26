import java.io.File

import com.typesafe.config.ConfigFactory

object SlfCrawler extends App {
  val config = ConfigFactory.load()
  var accessKeyId = config.getString("s3.accessKeyId")
  var secretAccessKey = config.getString("s3.secretAccessKey")
  var bucket = config.getString("s3.bucket")
  var dataFile = new File(config.getString("local.dataFile"))
  var indexFile = new File(config.getString("local.indexFile"))
  var tmpDir = new File(config.getString("local.tmpDir"))

  val index = Index.load(indexFile)

  val s3 = new S3(bucket, accessKeyId, secretAccessKey)

  // val newImages = crawlYear(s3, index, 2017)

  // Crawl current year
  val newImages = crawlYear(2018) flatMap { image =>
    loadImage(s3, index)(image)
  }

  val newIndex = index ++ newImages.map(_.s3Key)
  Index.save(indexFile)(newIndex)
  JsonData.saveToFile(dataFile, JsonData.fromIndex(newIndex))
  s3.save("data.json", dataFile)

  def crawlYear(year: Int): Seq[Image] = {
    val categories = Seq(Depth, DepthAt2000m, FreshSnow1Day, FreshSnow3Days, RelativeDepth)

    for {
      yearUrl <- SlfWebsite.yearArchive(year).toSeq
      category <- categories
      categoryUrl <- SlfWebsite.categoryArchive(yearUrl, year, category).toSeq
      languageUrl <- SlfWebsite.languageArchive(categoryUrl, year, category).toSeq
      fileType <- category.fileTypes(year)
      fileTypeUrl <- SlfWebsite.fileTypeArchive(languageUrl, year, category, fileType).toSeq
      image <- SlfWebsite.crawlImages(fileTypeUrl, year, category)
    } yield image
  }

  def loadImage(s3: S3, index: Set[String])(image: Image): Option[Image] = {
    if (!index.contains(image.s3Key)) {
      val tmpFile = SlfWebsite.downloadImage(image.url, tmpDir + "/" + image.name)
      s3.save(image.s3Key, tmpFile)
      tmpFile.delete()

      Some(image)
    } else {
      None
    }
  }
}
