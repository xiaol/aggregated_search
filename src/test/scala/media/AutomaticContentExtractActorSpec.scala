package media

// Created by ZG on 15/9/14.
//

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clients.media.MediaResult
import cores.{ExtractContentByAutomatic, AutomaticContentExtractActor}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class AutomaticContentExtractActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("AutomaticContentExtractActorSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An AutomaticContentExtractActor" must {

    "send back MediaResult extracted with url" in {
      val automaticContentExtractActor = system.actorOf(Props[AutomaticContentExtractActor], "AutomaticContentExtractActor-1")
      automaticContentExtractActor ! ExtractContentByAutomatic("http://news.163.com/15/0911/04/B374SE240001121M.html")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}
