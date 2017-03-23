import java.io.{File, PrintWriter}

import spray.json._
import DefaultJsonProtocol._

object JsonData {
  def fromIndex(index: Set[String]): JsValue = {
    val items = index.map(_.split('/').toList) groupBy {
      case List(year, _, _) => year
    } mapValues {
      _ groupBy {
        case List(_, category, _) => category
      } mapValues {
        _.toList.map{
          case List(_, _, name) => name
        }.sorted
      }
    }

    items.toJson
  }

  def saveToFile(file: File, json: JsValue): Unit = {
    val pw = new PrintWriter(file)
    pw.write(json.compactPrint)
    pw.close()
  }
}
