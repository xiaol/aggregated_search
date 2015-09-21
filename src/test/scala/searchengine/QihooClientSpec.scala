package searchengine

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.searchengine.{QihooClient, QihooItems, StartSearchEngineWithKey}

class QihooClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("QihooClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An QihooClient" must {

    "send back search result extracted with key" in {
      val baiduClient = system.actorOf(Props[QihooClient], "QihooClient-1")
      baiduClient ! StartSearchEngineWithKey("天津爆炸事故")
      within(100.second){
        expectMsgType[QihooItems]
      }
    }

  }

}
