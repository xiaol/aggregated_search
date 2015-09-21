package community

// Created by ZG on 15/9/10.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.community.{BaiduBaike, BaiduBaikeClient, ExtractBaiduBaike}

class BaiduBaikeClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("BaiduBaikeClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An BaiduBaikeClient" must {

    "send back BaiduBaike extracted with \"周杰伦\"" in {
      val baiduBaikeClient = system.actorOf(Props[BaiduBaikeClient], "BaiduBaikeClient-1")
      baiduBaikeClient ! ExtractBaiduBaike("周杰伦")
      within(100.second){
        expectMsg(BaiduBaike("周杰伦_百度百科",
          "http://baike.baidu.com/view/2632.htm",
          "周杰伦（Jay Chou），1979年1月18日出生于台湾新北市。" +
            "华语流行男歌手、词曲创作人、制作人、演员、MV及电影导演、编剧及监制1。2000年发..."))
      }
    }

    "send back an empty BaiduBaike extracted with the key not found" in {
      val baiduBaikeClient = system.actorOf(Props[BaiduBaikeClient], "BaiduBaikeClient-2")
      baiduBaikeClient ! ExtractBaiduBaike("")
      within(100.second){
        expectMsg(BaiduBaike("","",""))
      }
    }

  }

}