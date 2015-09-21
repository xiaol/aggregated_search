package nlps


// Created by ZG on 15/9/10.
//

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class NerExtractorActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("nlps.NerExtractorActorSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An NerExtractorActor" must {

    "send back ners extracted" in {
      val nerExtractor = system.actorOf(Props[NerExtractorActor], "NerExtractorActor")
      nerExtractor ! ExtractNers("人民日报：习近平给参训教师回信  周杰伦向全国教师致节日祝福")
      within(100.second){
        expectMsg(Ners(List(),List(),List(),List("周杰伦", "习近平"),List()))
      }
    }

  }

}
