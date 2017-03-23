import awscala._
import s3._

class S3(bucketName: String, accessKeyId: String, secretAccessKey: String) {
  private val s3 = S3(accessKeyId, secretAccessKey)(Region.Frankfurt)

  def save(key: String, file: File): Unit = {
    s3.putObject(bucketName, key, file)
  }
}
