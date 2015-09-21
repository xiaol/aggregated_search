package searchengine

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.searchengine.{SougouClient, SougouItems, StartSearchEngineWithKey}

class SougouClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SougouClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SougouClient" must {

    "send back search result extracted with key" in {
      val sougouClient = system.actorOf(Props[SougouClient], "SougouClient-1")
      sougouClient ! StartSearchEngineWithKey("天津爆炸事故")
      within(100.second){
        expectMsgType[SougouItems]
      }
    }

  }

}