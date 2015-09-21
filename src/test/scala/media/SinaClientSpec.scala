package media

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.media.{SinaClient, ExtractMedia, MediaResult}

class SinaClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SinaClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SinaClient" must {

    "send back MediaResult extracted with ExtractMedia" in {
      val sinaClient = system.actorOf(Props[SinaClient], "SinaClient-1")
      sinaClient ! ExtractMedia("http://finance.sina.cn/chanjing/rsbd/2015-09-11/detail-ifxhupkn4819786.d.html?vt=4&pos=108")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}