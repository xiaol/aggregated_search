package clients.searchengine

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import spray.client.pipelining._
import spray.http.HttpCharsets
import scala.util.{Failure, Success}
import utils.Agents
import spray.json._
import spray.json.DefaultJsonProtocol

case class BaiduItem(title:String, url:String)
case class BaiduItems(items: List[BaiduItem]) extends SearchResultItems

case class BaiduApiResultItem(title:String, url:String, author:String,
                              abs:String, sortTime:String, publicTime:String, imgUrl:String)
case class BaiduApiResultItems(items: List[BaiduApiResultItem])

object BaiduJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat7(BaiduApiResultItem.apply)
  implicit val itemsFormat = jsonFormat1(BaiduApiResultItems.apply)
}

class BaiduClient extends Actor {

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case StartSearchEngineWithKey(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://m.baidu.com/news?tn=bdapisearch&word=${java.net.URLEncoder.encode(key, "UTF-8")}&pn=0&rn=20")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))
      case Failure(error) => sender ! BaiduItems(List[BaiduItem]())
    }
  }

  def extractor(html:String) = {
    import BaiduJsonProtocol._
    val ApiItems = BaiduApiResultItems(html.parseJson.convertTo[List[BaiduApiResultItem]])
    val results:List[BaiduItem] = {
      for (item <- ApiItems.items) yield {
        BaiduItem(item.title, item.url)
      }
    }
    BaiduItems(results)
  }
}