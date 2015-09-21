package clients.searchengine

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import utils.Agents
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets

import scala.util.{Failure, Success}
import spray.json.DefaultJsonProtocol

case class SougouItem(title:String, url:String)
case class SougouItems(items: List[SougouItem]) extends SearchResultItems

object SougouJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat2(SougouItem.apply)
  implicit val itemsFormat = jsonFormat1(SougouItems.apply)
}

class SougouClient extends Actor{

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
      Get(s"http://news.sogou.com/news?oq=&mode=1&manual=&query=${java.net.URLEncoder.encode(key, "UTF-8")}&time=0&ri=0&sort=0&page=1")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! SougouItems(List[SougouItem]())
    }
  }

  def extractor(html:String) = {
    val doc = Jsoup.parse(html, "UTF-8")
    val links = doc.select("div[class=rb]")
    val results:Array[SougouItem] = {
      for (aTag <- links.toArray) yield {
        val url = Jsoup.parse(aTag.toString, "UTF-8").select("a[class=pp]").attr("href")
        SougouItem(Jsoup.parse(aTag.toString, "UTF-8").select("a[class=pp]").text().split("-")(0).split("_")(0),
          if(url.startsWith("http")) url else "http://news.sogou.com" + url)
      }
    }
    SougouItems(results.toList)
  }
}