package clients.searchengine

// Created by ZG on 15/7/22.
//

import utils.Agents
import akka.actor.{Actor, ActorRef}
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets
import scala.util.{Failure, Success}

import spray.json.DefaultJsonProtocol

case class SmItem(title:String, url:String)
case class SmItems(items: List[SmItem]) extends SearchResultItems

object SmJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat2(SmItem.apply)
  implicit val itemsFormat = jsonFormat1(SmItems.apply)
}

class SmClient extends Actor{
  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case StartSearchEngineWithKey(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.pc)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://hotnews.sm.cn/s?q=${java.net.URLEncoder.encode(key, "UTF-8")}&from=news&safe=1&by=submit&snum=0")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))
      case Failure(error) => sender ! SmItems(List[SmItem]())
    }
  }

  def extractor(html:String) = {
    val doc = Jsoup.parse(html, "UTF-8")
    val links = doc.select("div[class=article ali_row]")
    val results:Array[SmItem] = {
      for (aTag <- links.toArray) yield {
        SmItem(Jsoup.parse(aTag.toString, "UTF-8").select("h2").text().split("-")(0).split("_")(0),
          Jsoup.parse(aTag.toString, "UTF-8").select("a").first().attr("abs:href"))
      }
    }
    SmItems(results.toList)
  }
}