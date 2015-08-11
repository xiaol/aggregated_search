package com.search.clients.search

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import com.search._
import com.search.clients.tools.Agents
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets
import spray.httpx.encoding.Deflate

import scala.util.{Failure, Success}
import spray.json.DefaultJsonProtocol

case class QihooItem(title:String, url:String)
case class QihooItems(items: List[QihooItem])

object QihooJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat2(QihooItem.apply)
  implicit val itemsFormat = jsonFormat1(QihooItems.apply)
}

class QihooClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case StartSearchEngineWithKey(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.pc)
        ~> addHeader("Accept-Encoding", "charset=utf-8")
        ~> sendReceive
        ~> decode(Deflate)
      )
    val responseFuture = pipeline {
      Get(s"http://news.haosou.com/ns?q=${java.net.URLEncoder.encode(key, "UTF-8")}&rank=rank&src=srp&tn=news")
      // html
    }
    responseFuture onComplete {
      case Success(response) =>
        println("Qihoo result")
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! Error("qihoo")
    }
  }

  def extractor(html:String) = {
    val doc = Jsoup.parse(html, "UTF-8")
    val links = doc.select("ul[class=result]>li")
    val results:Array[QihooItem] = {
      for (aTag <- links.toArray) yield {
        val url = Jsoup.parse(aTag.toString, "UTF-8").select("a").attr("href")
        QihooItem(Jsoup.parse(aTag.toString, "UTF-8").select("h3").text().split("-")(0).split("_")(0),
          if(url.startsWith("http")) url else "http://m.news.haosou.com" + url)
      }
    }
    QihooItems(results.toList)
  }
}