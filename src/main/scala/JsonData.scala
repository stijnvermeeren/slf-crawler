import java.io.{File, PrintWriter}

import spray.json._
import DefaultJsonProtocol._

object JsonData {
  def fromIndex(index: Set[String]): JsValue = {
    val items = index.map(_.split('/').toList) groupBy {
      case List(category, _) => category
    } mapValues {
      _.toList.map{
        case List(_, name) =>
          name.dropRight(4) // drop extension
      }.sorted
    }

    items.toJson
  }

  def saveToFile(file: File, json: JsValue): Unit = {
    val pw = new PrintWriter(file)
    pw.write(json.compactPrint)
    pw.close()
  }
}
