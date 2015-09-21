package cores

// Created by ZG on 15/9/11.
//

import akka.actor.{ActorRef, Props, Actor}
import akka.event.Logging
import clients.media._
import utils.Base64Utils
import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

class TemplateContentExtractActor extends Actor{

  val log = Logging(context.system, this)

  // media service
  val sinaClient = context.actorOf(Props[SinaClient], "SinaClient")
  val sohuClient = context.actorOf(Props[SohuClient], "SohuClient")
  val shenmaClient = context.actorOf(Props[ShenmaClient], "ShenmaClient")
  val tencentClient = context.actorOf(Props[TencentClient], "TencentClient")
  val neteasyClient = context.actorOf(Props[NeteasyClient], "NeteasyClient")
  log.error("Init template content extractor server...")

  def receive = {
    case ExtractContentByTemplate(items) =>
      log.error(s"Start TemplateContentExtractActor with search items.")
      val originalSender = sender()

      context.actorOf(Props(new Actor() {

        var sendAlready = 0
        var receiveAlready = 0
        var resultList:ArrayBuffer[MediaResult] = ArrayBuffer()

        def receive = {
          case mediaResult:MediaResult =>
            if(mediaResult.newsContent.imageNum!=0) responseAndShutDown(mediaResult)
            else {receiveAlready += 1;  resultList.append(mediaResult); checkReadyReply()}

          case AccessTimeout =>
            log.error("Accesse timeout, return before all ready.")
            responseAndShutDown()

          case _ =>
        }

        def checkReadyReply() = {
          if(sendAlready==receiveAlready){
            log.error(s"In check reply, the urls has been send were all received, reply.")
            responseAndShutDown()
          }
        }

        def responseAndShutDown(mediaResult: MediaResult) = {
          log.error("Return with media result which has pictures, before all ready.")
          originalSender ! mediaResult.copy("TemplateExtractor")
          context.stop(self)
        }

        def responseAndShutDown() = {
          val completedResultList = resultList.filter(media => media.newsContent.url.nonEmpty)
          val mediaResult =
            if(completedResultList.nonEmpty) completedResultList.head
            else MediaResult("TemplateExtractor", NewsContent("","","","","","","",0,List[ContentBlock]()))
          log.error("Template extractor complete, return with media result.")
          originalSender ! mediaResult.copy("TemplateExtractor")
          context.stop(self)
        }

        for(item <- items.searchItems){
          item match {
            case SearchItem(url, title) if item.url.contains("zzd.sm.cn/") =>
              sendAlready += 1
              shenmaClient     ! ExtractMedia(url)
            case SearchItem(url, title) if item.url.contains("163.com/") =>
              sendAlready += 1
              neteasyClient    ! ExtractMedia(url)
            case SearchItem(url, title) if item.url.contains("sohu.com/") =>
              sendAlready += 1
              sohuClient       ! ExtractMedia(url)
            case SearchItem(url, title) if item.url.contains("sina.com") =>
              sendAlready += 1
              sinaClient       ! ExtractMedia(url)
            case SearchItem(url, title) if item.url.contains("qq.com/") =>
              sendAlready += 1
              tencentClient    ! ExtractMedia(url)

            case _ =>
          }
        }

        import context.dispatcher
        val timeoutMessager = context.system.scheduler.scheduleOnce(60.seconds, self, AccessTimeout)

      }))

    case _ =>
  }
}

class TemplateContentExtractServer(templateContentExtractClient: ActorRef) extends Actor{

  def receive = {
    case ExtractContentByTemplateWithUrls(urlList) =>
      templateContentExtractClient ! ExtractContentByTemplate(
        SearchItems({for(url <- Base64Utils.DecodeBase64(urlList).split("&&")) yield SearchItem(url,"")}.toList))
    case mediaResult:MediaResult => context.parent ! mediaResult
    case _ =>
  }

}
