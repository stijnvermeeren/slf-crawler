import awscala._
import com.amazonaws.services.s3.model.GetObjectRequest
import s3._

class S3(bucketName: String, accessKeyId: String, secretAccessKey: String) {
  private val s3 = S3(accessKeyId, secretAccessKey)(Region.Frankfurt)

  def save(key: String, file: File): Unit = {
    s3.putObject(bucketName, key, file)
  }

  def load(key: String, file: File): Unit = {
    s3.getObject(new GetObjectRequest(bucketName, key), file)
  }

  def copy(oldKey: String, newKey: String): Unit = {
    s3.copyObject(bucketName, oldKey, bucketName, newKey)
  }
}
