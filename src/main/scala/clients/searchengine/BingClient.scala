package clients.searchengine

// Created by ZG on 15/7/20.
// 

import akka.actor.{Actor, ActorRef}
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets
import utils.Agents
import scala.util.{Failure, Success}
import spray.json.DefaultJsonProtocol

case class BingItem(title:String, url:String)
case class BingItems(items: List[BingItem]) extends SearchResultItems

object BingJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat2(BingItem.apply)
  implicit val itemsFormat = jsonFormat1(BingItems.apply)
}

class BingClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case StartSearchEngineWithKey(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline = (
      addHeader("User-agent", Agents.pc)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://cn.bing.com/search?q=${java.net.URLEncoder.encode(key, "UTF-8")}")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! BingItems(List[BingItem]())
    }
  }

  def extractor(html:String) = {
    val doc = Jsoup.parse(html, "UTF-8")
    val links = doc.select("ol[id=b_results]>li>h2>a")
    val results:Array[BingItem] = {
      for (aTag <- links.toArray) yield {
        BingItem(Jsoup.parse(aTag.toString, "UTF-8").text().split("-")(0).split("_")(0),
          Jsoup.parse(aTag.toString, "UTF-8").select("a").attr("abs:href"))
      }
    }
    BingItems(results.toList)
  }
}