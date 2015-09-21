package media

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.media.{TencentClient, ExtractMedia, MediaResult}

class TencentClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("TencentClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An TencentClient" must {

    "send back MediaResult extracted with ExtractMedia" in {
      val tencentClient = system.actorOf(Props[TencentClient], "TencentClient-1")
      tencentClient ! ExtractMedia("http://info.3g.qq.com/g/s?icfa=news_01&aid=min_ss&id=min_20150911025537&pos=news_gnnewstt&i_f=756")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}
