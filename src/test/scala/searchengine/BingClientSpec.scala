package searchengine

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.searchengine.{BingClient, BingItems, StartSearchEngineWithKey}

class BingClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("BingClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An BingClient" must {

    "send back search result extracted with key" in {
      val bingClient = system.actorOf(Props[BingClient], "BingClient-1")
      bingClient ! StartSearchEngineWithKey("天津爆炸事故")
      within(100.second){
        expectMsgType[BingItems]
      }
    }

  }

}