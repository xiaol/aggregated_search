
// Created by ZG on 15/9/15.
//

import akka.actor.{ActorSystem, Props}

import nlps._
import cores._
import clients.media._
import clients.community._

object TestWeiboCommentsActor extends App{

  implicit val system = ActorSystem("TestWeiboCommentsActor")

  val sinaWeiboCookiesClient = system.actorOf(Props[SinaWeiboCookiesClient], "SinaWeiboCookiesClient")
  val sinaWeiboCommentClient = system.actorOf(Props[SinaWeiboCommentClient], "SinaWeiboCommentClient")

  val sinaWeiboCommentsClient = system.actorOf(Props[SinaWeiboCommentsClient], "SinaWeiboCommentsClient")

  sinaWeiboCommentsClient ! ExtractWeiboComments(Weibos(List(
    Weibo("http://m.weibo.cn/3217179555/CACQUbjTV","","","","","","","","",List[String]()),
    Weibo("http://m.weibo.cn/1644395354/CACSb3GF0","","","","","","","","",List[String]()),
    Weibo("http://m.weibo.cn/1402400261/CADqbpqXX","","","","","","","","",List[String]()))
  ))

  Thread.sleep(1000 * 30)
  system.shutdown()

}
