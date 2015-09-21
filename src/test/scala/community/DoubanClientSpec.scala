package community

// Created by ZG on 15/9/10.
//

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{Douban, DoubanClient, ExtractDouban}

class DoubanClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("DoubanClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An DoubanClientSpec" must {

    "send back Douban extracted with \"周杰伦\"" in {
      val doubanlient = system.actorOf(Props[DoubanClient], "DoubanClient-1")
      doubanlient ! ExtractDouban("周杰伦")
      within(100.second){
        expectMsg(Douban("周杰伦",
          "http://www.douban.com/link2/?url=http%3A%2F%2Fwww.douban.com%2Fgroup%2F293845%2F&" +
            "query=%E5%91%A8%E6%9D%B0%E4%BC%A6&cat_id=1019&type=search&pos=0"))
      }
    }

    "send back an empty Douban extracted with the key not found" in {
      val doubanlient = system.actorOf(Props[DoubanClient], "DoubanClient-2")
      doubanlient ! ExtractDouban("")
      within(100.second){
        expectMsg(Douban("",""))
      }
    }

  }

}
