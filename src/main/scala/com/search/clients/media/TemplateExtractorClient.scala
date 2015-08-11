package com.search.clients.media

// Created by ZG on 15/8/4.
//

import akka.actor.{Props, Actor}
import com.search._

import scala.collection.mutable.ArrayBuffer

class TemplateExtractorClient extends Actor{

  // media service
  val neteasyService = context.actorOf(Props[NeteasyClient])
  val sinaService = context.actorOf(Props[SinaClient])
  val sohuService = context.actorOf(Props[SohuClient])
  val tencentService = context.actorOf(Props[TencentClient])
  val shenmaService = context.actorOf(Props[ShenmaClient])

  var sendAlready = 0
  var receiveAlready = 0
  var resultList:ArrayBuffer[ExtractResult] = ArrayBuffer()


  var info:ExcavatorInfo = _

  def receive = {
    case ExtractWithSearchItems(excavatorInfo, searchItems) =>
      println("Start Template Extractor!!!")
      info = excavatorInfo
      startMediaServices(searchItems)

    case extractResult:ExtractResult =>
      if(extractResult.newsContent.imageNum != 0)
        context.parent ! TemplateExtractResult(info, extractResult)
      else {
        receiveAlready += 1
        resultList += extractResult
        println(s"Not already, sendAlready=$sendAlready,receiveAlready=$receiveAlready")
        checkReply()
      }

    case _ =>
      receiveAlready += 1
      checkReply()
  }

  private def startMediaServices(searchItems:List[UrlMapTitleItem]) = {
    for(item <- searchItems){
      item match {
        case UrlMapTitleItem(url, title) if item.url.contains("163.com/") =>
          sendAlready += 1
          neteasyService    ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("sohu.com/") =>
          sendAlready += 1
          sohuService       ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("sina.com") =>
          sendAlready += 1
          sinaService       ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("qq.com/") =>
          sendAlready += 1
          tencentService    ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("zzd.sm.cn/") =>
          sendAlready += 1
          shenmaService     ! StartExtractMediaWithUrl(url)
        case _ =>
      }
    }
  }

  private def checkReply() = {
    if(sendAlready==receiveAlready){
      context.parent ! TemplateExtractResult(info, resultList.head)
    }
  }
}
