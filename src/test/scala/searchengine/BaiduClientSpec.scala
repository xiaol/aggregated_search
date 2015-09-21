package searchengine

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.searchengine.{BaiduClient, BaiduItems, StartSearchEngineWithKey}

class BaiduClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("BaiduClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An BaiduClient" must {

    "send back search result extracted with key" in {
      val baiduClient = system.actorOf(Props[BaiduClient], "BaiduClient-1")
      baiduClient ! StartSearchEngineWithKey("天津爆炸事故")
      within(100.second){
        expectMsgType[BaiduItems]
      }
    }

  }

}