package com.search.clients.search

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import spray.client.pipelining._

import scala.util.{Failure, Success}

import com.search.clients.search.YahooClient._

class YahooClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case SearchYahooByKey(key) =>
      process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline = sendReceive
    val responseFuture = pipeline {
      Get(s"https://search.yahoo.com/search?ei=UTF-8&p=$key")
      // html
    }
    responseFuture onComplete {
      case Success(status) => sender ! YahooResult(status.message.entity.toString)

      case Failure(error) => sender ! YahooResult("None")
    }
  }
}

object YahooClient{
  case class SearchYahooByKey(key: String)
  case class YahooResult(result: String)
}