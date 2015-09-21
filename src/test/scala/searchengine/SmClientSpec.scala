package searchengine

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.searchengine.{SmClient, SmItems, StartSearchEngineWithKey}

class SmClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SmClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SmClient" must {

    "send back search result extracted with key" in {
      val smClient = system.actorOf(Props[SmClient], "SmClient-1")
      smClient ! StartSearchEngineWithKey("天津爆炸事故")
      within(100.second){
        expectMsgType[SmItems]
      }
    }

  }

}