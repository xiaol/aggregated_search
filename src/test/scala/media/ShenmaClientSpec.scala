package media

// Created by ZG on 15/9/11.
// 

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import clients.media.{ShenmaClient, ExtractMedia, MediaResult}

class ShenmaClientSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  def this() = this(ActorSystem("ShenmaClientSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "An ShenmaClient" must {

    "send back MediaResult extracted with ExtractMedia" in {
      val shenmaClient = system.actorOf(Props[ShenmaClient], "ShenmaClient-1")
      shenmaClient ! ExtractMedia("http://zzd.sm.cn/webapp/webview/article/11435435795340736168?app=webapp&uc_param_str=dnnivebichfrmintcpgieiwidsud&ve=1.8.0.0&sn=11684633143300434651&cid=100&zzd_from=webapp&rd_type=reco&rc_id=4854161279746427565&refrd_id=&type=normal&recoid=4854161279746427565&aid=11435435795340736168")
      within(100.second){
        expectMsgType[MediaResult]
      }
    }

  }

}