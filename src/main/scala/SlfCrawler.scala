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

  // Crawl current year
  val newImages = crawlYear(2018) flatMap { image =>
    loadImage(s3, index)(image)
  }

  val newIndex = index ++ newImages.map(_.s3Key)
  Index.save(indexFile)(newIndex)
  JsonData.saveToFile(dataFile, JsonData.fromIndex(newIndex))
  // s3.save("data.json", dataFile)

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
      val tmpFile = File.createTempFile(s"${image.category}_${image.dateString}", s".${image.extension}")
      val tmpOptimisedFile = File.createTempFile(s"optimised_${image.category}_${image.dateString}", ".png")
      val tmpThumbFile = File.createTempFile(s"thumb_${image.category}_${image.dateString}", ".png")

      SlfWebsite.downloadImage(image.url, tmpFile.getPath)
      ImageOptimisation.optimise(tmpFile.getPath, tmpOptimisedFile.getPath, resizeWidth = None)
      ImageOptimisation.optimise(tmpFile.getPath, tmpThumbFile.getPath, resizeWidth = Some(100))

      s3.save(image.s3Key, tmpFile)
      s3.save(image.s3KeyOptimised, tmpOptimisedFile)
      s3.save(image.s3KeyThumb, tmpThumbFile)

      tmpFile.delete()
      tmpOptimisedFile.delete()
      tmpThumbFile.delete()

      Some(image)
    } else {
      None
    }
  }
}
