package com.search.clients.media

// Created by ZG on 15/8/4.
//

import akka.actor.{Props, Actor}
import com.search._

class TemplateExtractorClient extends Actor{

  // media service
  val neteasyService = context.actorOf(Props[NeteasyClient])
  val sinaService = context.actorOf(Props[SinaClient])
  val sohuService = context.actorOf(Props[SohuClient])
  val tencentService = context.actorOf(Props[TencentClient])
  val shenmaService = context.actorOf(Props[ShenmaClient])

  var info:ExcavatorInfo = _

  def receive = {
    case ExtractWithSearchItems(excavatorInfo, searchItems) =>
      println("Start Template Extractor!!!")
      info = excavatorInfo
      startMediaServices(searchItems)

    case extractResult:ExtractResult =>
      context.parent ! TemplateExtractResult(info, extractResult)

    case _ =>
  }

  private def startMediaServices(searchItems:List[UrlMapTitleItem]) = {
    var neteasyFlag = true
    var sinaFlag = true
    var sohuFlag = true
    var tencentFlag = true
    var shenmaFlag = true

    for(item <- searchItems){
      item match {
        case UrlMapTitleItem(url, title) if item.url.contains("163.com/") && neteasyFlag =>
          neteasyFlag = false
          neteasyService    ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("sohu.com/") && sohuFlag =>
          sohuFlag = false
          sohuService       ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("sina.com") && sinaFlag =>
          sinaFlag = false
          sinaService       ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("qq.com/") && tencentFlag =>
          tencentFlag = false
          tencentService    ! StartExtractMediaWithUrl(url)
        case UrlMapTitleItem(url, title) if item.url.contains("zzd.sm.cn/") && shenmaFlag =>
          shenmaFlag = false
          shenmaService     ! StartExtractMediaWithUrl(url)
        case _ =>
      }
    }
  }
}
