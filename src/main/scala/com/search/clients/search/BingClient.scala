package com.search.clients.search

// Created by ZG on 15/7/20.
// 

import akka.actor.{Actor, ActorRef}
import com.search._
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets

import scala.util.{Failure, Success}

import com.search.clients.search.BingClient._

import spray.json.DefaultJsonProtocol

case class BingItem(title:String, url:String)
case class BingItems(items: List[BingItem])

object BingJsonProtocol extends DefaultJsonProtocol{
  implicit val itemFormat = jsonFormat2(BingItem.apply)
  implicit val itemsFormat = jsonFormat1(BingItems.apply)
}

class BingClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case SearchBingByKey(key) =>
      process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline = sendReceive
    val responseFuture = pipeline {
      Get(s"http://cn.bing.com/search?q=${java.net.URLEncoder.encode(key, "UTF-8")}")
      // html
    }
    responseFuture onComplete {
      case Success(response) =>
        println("Bing result")
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! Error("bing")
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

object BingClient{
  case class SearchBingByKey(key: String)
  case class BingResult(result: String)
}