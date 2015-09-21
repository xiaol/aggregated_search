package media

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.media.{SohuClient, ExtractMedia, MediaResult}

class SohuClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SohuClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SohuClient" must {

    "send back MediaResult extracted with ExtractMedia" in {
      val sohuClient = system.actorOf(Props[SohuClient], "SohuClient-1")
      sohuClient ! ExtractMedia("http://m.sohu.com/n/420896186/?wscrid=85817_2")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}