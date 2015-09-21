package community

// Created by ZG on 15/9/10.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{GetWeiboCookie, SinaWeiboCookiesClient, Cookie}

class SinaWeiboCookiesClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SinaWeiboCookiesClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SinaWeiboCookiesClient" must {

    "send back Cookie extracted with GetWeiboCookie" in {
      val sinaWeiboCookiesClient = system.actorOf(Props[SinaWeiboCookiesClient], "SinaWeiboCookiesClient-1")
      sinaWeiboCookiesClient ! GetWeiboCookie
      within(100.second){
        expectMsgType[Cookie]
      }
    }

  }

}