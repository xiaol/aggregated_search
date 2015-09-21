package community

// Created by ZG on 15/9/10.
//

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{Weibos, SinaWeiboClient, ExtractWeibos}

class SinaWeiboClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SinaWeiboClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SinaWeiboClient" must {

    "send back weibos extracted with key" in {
      val sinaWeiboClient = system.actorOf(Props[SinaWeiboClient], "SinaWeiboClient-1")
      sinaWeiboClient ! ExtractWeibos("强制拆迁自焚")
      within(100.second){
        expectMsgType[Weibos]
      }
    }
  }

}