package community

// Created by ZG on 15/9/10.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{ZhihuClient, Zhihus, ExtractZhihus}

class ZhihuClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("ZhihuClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An ZhihuClient" must {

    "send back Zhihus extracted with \"周杰伦\"" in {
      val zhihuClient = system.actorOf(Props[ZhihuClient], "ZhihuClient-1")
      zhihuClient ! ExtractZhihus("周杰伦")
      within(100.second){
        expectMsgType[Zhihus]
      }
    }

  }

}
