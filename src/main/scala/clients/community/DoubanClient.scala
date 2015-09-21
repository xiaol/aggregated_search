package clients.community

// Created by ZG on 15/8/21.
//

import akka.actor.{Actor, ActorRef}
import scala.util.{Failure, Success}
import utils.{Extractor, Agents}
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets

case class ExtractDouban(key:String)
case class Douban(title:String, url:String)

class DoubanClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractDouban(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://www.douban.com/search?cat=1019&q=${java.net.URLEncoder.encode(key,"UTF-8")}")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(key,response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! Douban("","")
    }
  }

  def extractor(key:String, html:String) ={
    var url:String = ""

    val doc = Jsoup.parse(html, "UTF-8")

    val searchResultLists = Extractor.getMentBySels(doc, List("div[class=result-list]"))
    if(searchResultLists!=null){
      val searchResult = searchResultLists.select("h3").first()
      if(searchResult!=null){
        val a = searchResult.getElementsByTag("a")
        if(a!=null){
          url = a.attr("href")
        }
      }
    }
    if(url.nonEmpty) Douban(key, url) else Douban("","")
  }

}
