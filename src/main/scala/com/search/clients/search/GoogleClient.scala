package com.search.clients.search

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import spray.client.pipelining._

import scala.util.{Failure, Success}

import com.search.clients.search.GoogleClient._

class GoogleClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case SearchGoogleByKey(key) =>
      process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline = sendReceive
    val responseFuture = pipeline {
      Get(s"http://news.sogou.com/news/wap/searchlist_ajax.jsp?keyword=$key&from=ajax&page=1")
    }
    responseFuture onComplete {
      case Success(status) => sender ! GoogleResult(status.message.entity.toString)

      case Failure(error) => sender ! GoogleResult("None")
    }
  }
}

object GoogleClient{
  case class SearchGoogleByKey(key: String)
  case class GoogleResult(result: String)
}