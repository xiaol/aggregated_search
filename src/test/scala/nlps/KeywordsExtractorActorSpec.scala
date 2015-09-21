package nlps


// Created by ZG on 15/9/9.
//

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class KeywordsExtractorActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("nlps.KeywordsExtractorActorSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An KeywordsExtractor" must {

    "send back keywords extracted" in {
      val keywordsExtractor = system.actorOf(Props[KeywordsExtractorActor], "KeywordsExtractorActor")
      keywordsExtractor ! ExtractKeywords("APPLE发布会最新发布Iphone 6s")
      within(20.second){
        expectMsg(Keywords(List("APPLE", "Iphone", "发布会", "最新", "发布")))
      }
    }

  }





}
