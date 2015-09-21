package clients.community

// Created by ZG on 15/8/21.
//

import akka.actor.{Actor, ActorRef}
import spray.client.pipelining._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.json4s._
import org.json4s.native.JsonMethods._

import utils.{Agents, DateUtils}

case class ExtractWeibos(key:String)
case class Weibo(url:String, content:String, updateTime:String, sourceName:String,
                 likesCount:String, repostsCount:String, commentsCount:String, profileImgUrl:String,
                 imgUrl:String, imgUrls:List[String])
case class Weibos(weibos:List[Weibo])

class SinaWeiboClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractWeibos(key) =>
      process(key, sender())
  }

  def process(title: String, sender: ActorRef) = {
    val rt = java.net.URLEncoder.encode(title.replaceAll("[\\pP\\pS]", ""), "UTF-8")

    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://m.weibo.cn/page/pageJson?containerid=&containerid=100103type%3D1%26q%3D$rt&" +
        s"type=all&queryVal=$rt&luicode=20000174&title=$rt&v_p=11&ext=&fid=100103type%3D1%26q%" +
        s"3D$rt&uicode=10000011&page=1&xsort=hot")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(title, response.entity.asString)

      case Failure(error) =>
        sender ! Weibos(List[Weibo]())
    }
  }

  def extractor(title: String, html:String) = {

    implicit val formats = Serialization.formats(NoTypeHints)

    val weiboList:ArrayBuffer[Weibo] = ArrayBuffer()

    val parseCards = parse(html) \\ "cards"

    if(parseCards!=JNothing) {
      for (cardGroups <- parseCards.children.filter(x =>
        x.\("card_type") != JNothing &&
          x.\("card_type").values != 16 &&
          x.\("card_group") != JNothing)) {
        for (card <- cardGroups.\("card_group").children.filter(y =>
          y.\("scheme").isInstanceOf[JString] &&
            y.\("mblog").isInstanceOf[JObject])
        ) {
          val wbUrl = card.\("scheme").values.toString
          val wbMblog = card.\("mblog")
          val wbLikeCount = wbMblog.\("like_count").values.toString
          val wbCommentsCount = wbMblog.\("comments_count").values.toString
          val wbRepostsCount = wbMblog.\("reposts_count").values.toString
          val wbContent = wbMblog.\("text").values.toString.replaceAll("<[\\s\\S]+?>", "")
          val wbUpdateTime = wbMblog.\("created_at").values.toString match {
            case x:String if x.contains("\u5206\u949f\u524d") || x.contains("\u79d2\u949f\u524d") => DateUtils.getCurrentDate
            case x:String if x.contains("\u4eca\u5929") =>  x.replace("\u4eca\u5929", DateUtils.getCurrentDay)
            case x => x
          }
          val wbPics = wbMblog.\("pics")
          val picUrls = if(wbPics != JNothing) {for(pic <- wbPics.children if pic.\("url") != JNothing) yield pic.\("url").values.toString} else List()
          val picUrl = if(picUrls.nonEmpty) picUrls.head else ""
          val wbUser = wbMblog.\("user")
          val wbScreenName = wbUser.\("screen_name").values.toString
          val wbProfileImgUrl = wbUser.\("profile_image_url").values.toString

          val weibo = Weibo(
            wbUrl,
            wbContent,
            wbUpdateTime,
            wbScreenName,
            wbLikeCount,
            wbRepostsCount,
            wbCommentsCount,
            wbProfileImgUrl,
            picUrl,
            picUrls
          )
//          println(s"weibo: $weibo")
          weiboList.append(weibo)

//          val weiboSer = write(weibo)
//          println(s"weiboSer: $weiboSer")
//
//          val readWeiboSer = read[Weibo](weiboSer)
//          println(s"readWeiboSer: $readWeiboSer")

//          println("*"*100)
        }
      }
    }

    Weibos(weiboList.toList)
  }

}
