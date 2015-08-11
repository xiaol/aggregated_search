package com.search.core

// Created by ZG on 15/7/20.
//

import akka.actor.{Props, ActorRef, Actor, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate

import com.search._
import com.search.clients.search._

class AggregateSearchWithKeyActor(baiduService: ActorRef, bingService: ActorRef, qihooService: ActorRef,
                              smService:ActorRef, sougouService: ActorRef) extends Actor {

  var searchKey:String = ""
  var errList:String = ""

  var baiduItems:List[BaiduItem] = null
  var bingItems:List[BingItem] = null
  var qihooItems:List[QihooItem] = null
  var smItems:List[SmItem] = null
  var sougouItems:List[SougouItem] = null

  def receive = {
    case SearchWithKey(key) =>
      searchKey = key
      baiduService  ! StartSearchEngineWithKey(key)
      bingService   ! StartSearchEngineWithKey(key)
      qihooService  ! StartSearchEngineWithKey(key)
      smService     ! StartSearchEngineWithKey(key)
      sougouService ! StartSearchEngineWithKey(key)

      context.become(waitingResponses)
  }

  def waitingResponses: Receive = {

    case BaiduItems(items) => {
      baiduItems = items
      replyIfReady()
    }
    case BingItems(items) => {
      bingItems = items
      replyIfReady()
    }
    case QihooItems(items) => {
      qihooItems = items
      replyIfReady()
    }
    case SmItems(items) => {
      smItems = items
      replyIfReady()
    }
    case SougouItems(items) => {
      sougouItems = items
      replyIfReady()
    }
    case Error(errMessage) =>
      errList = errList + errMessage
      replyIfReady()

    case f: Validation => context.parent ! f
  }

  def replyIfReady() =
    if((baiduItems!=null || errList.contains("baidu"))
        && (bingItems!=null || errList.contains("bing"))
        && (qihooItems!=null || errList.contains("qihoo"))
        && (smItems!=null || errList.contains("sm"))
        && (sougouItems!=null || errList.contains("sougou"))
    ) {
      var urlSet:Set[String] = Set()
      var urlMapTitleItems:List[UrlMapTitleItem] = List()

      if(baiduItems!=null){
        for(item <- baiduItems) if(!urlSet.contains(item.url) && item.url.startsWith("http")) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(bingItems!=null){
        for(item <- bingItems) if(!urlSet.contains(item.url) && item.url.startsWith("http")) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(qihooItems!=null){
        for(item <- qihooItems) if(!urlSet.contains(item.url) && item.url.startsWith("http")) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(smItems!=null){
        for(item <- smItems) if(!urlSet.contains(item.url) && item.url.startsWith("http")) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(sougouItems!=null){
        for(item <- sougouItems) if(!urlSet.contains(item.url) && item.url.startsWith("http")) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
//      urlMapTitleItems.foreach(item => println(item.title, item.url))


      // return to complete
//      val results = for(item <- urlMapTitleItems) yield s"${item.url}=>${item.title}"
//      val results = for(item <- urlMapTitleItems if{item.url.contains("163.com/") ||
//        item.url.contains("sohu.com/") ||
//        item.url.contains("sina.com") ||
//        item.url.contains("qq.com/") ||
//        item.url.contains("zzd.sm.cn/")}) yield s"${item.url}"
//      import com.search.clients.tools.Base64Utils
//      context.parent ! TestResults(Base64Utils.EncodeBase64(results.slice(0,10).mkString("&&")))
      context.parent ! UrlMapTitleItems(urlMapTitleItems)
    }
}
