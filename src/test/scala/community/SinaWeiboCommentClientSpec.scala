package community

// Created by ZG on 15/9/10.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{SinaWeiboCommentClient, ExtractWeiboCommentsWithUrlAndCookie, WeiboComments}

class SinaWeiboCommentClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("SinaWeiboCommentClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An SinaWeiboCommentClient" must {

    "send back WeiboComments extracted with WeiboUrl" in {
      val sinaWeiboCommentClient = system.actorOf(Props[SinaWeiboCommentClient], "SinaWeiboCommentClient-1")
      sinaWeiboCommentClient ! ExtractWeiboCommentsWithUrlAndCookie(
        "http://m.weibo.cn/1580904460/CzWbck9mu",
        "SUB=_2A2549eA9DeTxGeVG71oY9S7OyjuIHXVYGYB1rDV6PUJbrdAKLRTckW06dBBR6L7lE-iaNbPsaBqPfs_-OA..;")
      within(100.second){
        expectMsgType[WeiboComments]
      }
    }

  }

}