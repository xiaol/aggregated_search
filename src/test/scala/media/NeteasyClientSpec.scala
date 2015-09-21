package media

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.media.{NeteasyClient, ExtractMedia, MediaResult}

class NeteasyClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("NeteasyClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An NeteasyClient" must {

    "send back MediaResult extracted with ExtractMedia" in {
      val neteasyClient = system.actorOf(Props[NeteasyClient], "NeteasyClient-1")
      neteasyClient ! ExtractMedia("http://news.163.com/15/0911/04/B374SE240001121M.html")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}