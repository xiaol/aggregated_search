package clients.community

// Created by ZG on 15/8/31.
//

import akka.actor.{Props, ActorRef, Actor}
import cores.AccessTimeout
import org.json4s._
import org.json4s.native.JsonMethods._
import reactivemongo.bson.BSONObjectID
import spray.client.pipelining._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}
import akka.event.Logging
import scala.concurrent.duration._
import utils.{Base62Utils, DateUtils, Agents}

case class ExtractWeiboComments(weibos:Weibos)
case class ExtractWeiboCommentsWithUrlAndCookie(url:String, cookie:String)
case class WeiboComment(createTime:String, upCount:String, authorName:String, authorId:String,
                        authorImgUrl:String, weiboId:String, commentId:String, message:String)
case class WeiboComments(comments:List[WeiboComment])

class SinaWeiboCommentClient extends Actor{

      implicit val system = context.system
      import system.dispatcher
      val log = Logging(context.system, this)

      def receive = {
        case ExtractWeiboCommentsWithUrlAndCookie(weiboUrl, cookie) if weiboUrl.startsWith("http://m.weibo.cn/") =>
//          log.error("Accept a weibo url, start to get comments with it's userID and newsID.")
          val ids = weiboUrl.replace("http://m.weibo.cn/", "").replace("?", "").trim.split("/")
          processHotComments(ids.head, Base62Utils.weiboMid2Id(ids.last), cookie, sender())

    case _ =>
//      log.error("Unknow type of weibo url, check your weibo url.")
      sender ! WeiboComments(List[WeiboComment]())
  }

  def processHotComments(userID:String, newsID: String, cookie:String, sender: ActorRef) = {
    val apiHot = "http://m.weibo.cn/single/rcList?id=NewsID&type=comment&hot=1&tab=1&format=cards"
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> addHeader("Cookie", cookie)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(apiHot.replace("NewsID", newsID).replace("UserID", userID))
    }
    responseFuture onComplete{
      case Success(response) =>
        val comments = commentsExtractor(userID, newsID, response.entity.asString)
        if(comments.comments.nonEmpty) {
//          log.error(s"Successful, Hot comments get with userID: $userID, newsID: $newsID.")
          sender ! comments}
        else processComComments(userID, newsID, cookie, sender)

      case Failure(error) => processComComments(userID, newsID, cookie, sender)
    }
  }

  def processComComments(userID:String, newsID: String, cookie:String, sender: ActorRef) = {
    val apiCom = "http://m.weibo.cn/UserID/NewsID/rcMod?format=cards&type=comment&hot=1"
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> addHeader("Cookie", cookie)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(apiCom.replace("NewsID", newsID).replace("UserID", userID))
    }
    responseFuture onComplete{
      case Success(response) =>
//        log.error(s"Successful, Common comments get with userID: $userID, newsID: $newsID.")
        sender ! commentsExtractor(userID, newsID, response.entity.asString)
      case Failure(error) =>
//        log.error(s"Faield, Common comments get with userID: $userID, newsID: $newsID.")
        sender ! WeiboComments(List[WeiboComment]())
    }
  }

  def commentsExtractor(userID: String, newsID: String, weiboHtml:String) = {
    val weiboCommentList:ArrayBuffer[WeiboComment] = ArrayBuffer()
    val parseCards = parse(weiboHtml) \\ "card_group"
    if(parseCards!=JNothing){
      for(card <- parseCards.children if card.isInstanceOf[JObject]){
        val createTime = card.\("created_at").values.toString match {
          case x:String if x.contains("\u5206\u949f\u524d") || x.contains("\u79d2\u949f\u524d") => DateUtils.getCurrentDate
          case x:String if x.contains("\u4eca\u5929") =>  x.replace("\u4eca\u5929", DateUtils.getCurrentDay)
          case x => x
        }
        val upCount = card.\("like_counts").values.toString
        val message = card.\("text").values.toString.replaceAll("<[\\s\\S]+?>", "")
        val commentId = BSONObjectID.generate.stringify
        val userCard = card.\("user")
        if(userCard!=JNothing){
          val authorName = userCard.\("screen_name").values.toString
          val authorId = userCard.\("id").values.toString
          val authorImgUrl = userCard.\("profile_image_url").values.toString

          val comment = WeiboComment(createTime, upCount, authorName, authorId, authorImgUrl, newsID, commentId, message)
          weiboCommentList.append(comment)
        }
      }
    }
    WeiboComments(weiboCommentList.toList)
  }
}

class SinaWeiboCommentsClient extends Actor{

  val log = Logging(context.system, this)

  val weiboCookiesServer = context.actorOf(Props[SinaWeiboCookiesClient], "SinaWeiboCookiesClient")
  val weiboCommentServer = context.actorOf(Props[SinaWeiboCommentClient], "SinaWeiboCommentClient")
  log.error("Init weibo comments extractor server...")

  def receive = {
    case ExtractWeiboComments(weiboList) =>
      log.error("Start excavator weibo comments with weibo list.")
      val originalSender = sender()
      context.actorOf(Props(new Actor() {

        val commentsResultList:ArrayBuffer[WeiboComments] = ArrayBuffer()

        def receive = {
          case Cookie(cookie) => weiboList.weibos.foreach{ weibo =>
            weiboCommentServer ! ExtractWeiboCommentsWithUrlAndCookie(weibo.url, cookie)
          }
          case weiboComments:WeiboComments =>
            commentsResultList.append(weiboComments)
            checkReadyReply()
          case AccessTimeout =>
            log.error("Comments extract timeout, reply.")
            responseAndShutDown()
          case _ =>
        }

        def checkReadyReply() = {
          if(commentsResultList.size == weiboList.weibos.size){
            log.error("All comments get, reply.")
            responseAndShutDown()
          }
        }

        def responseAndShutDown() = {
          val commentsResult = for{
            commentsList <- commentsResultList if commentsList.comments.nonEmpty
            comment <- commentsList.comments
          } yield comment

          originalSender ! WeiboComments(commentsResult.toList)
          context.stop(self)
        }

        weiboCookiesServer ! GetWeiboCookie

        import context.dispatcher
        val timeoutMessager = context.system.scheduler.scheduleOnce(100.seconds, self, AccessTimeout)

      }))

    case _ =>
  }

}