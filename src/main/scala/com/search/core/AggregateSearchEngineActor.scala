package com.search.core

// Created by ZG on 15/8/3.
//

import akka.actor.{ActorRef, Props, Actor}

import com.search._
import com.search.clients.search._

class AggregateSearchEngineActor extends Actor{

  //
  var aggregateSearchTask:AggregateSearchTask = _

  //
  var errList:String = ""
  var baiduItems:List[BaiduItem] = _
  var bingItems:List[BingItem] = _
  var qihooItems:List[QihooItem] = _
  var smItems:List[SmItem] = _
  var sougouItems:List[SougouItem] = _

  // search service
  val baiduService = context.actorOf(Props[BaiduClient])
  val bingService = context.actorOf(Props[BingClient])
  val qihooService = context.actorOf(Props[QihooClient])
  val smService = context.actorOf(Props[SmClient])
  val sougouService = context.actorOf(Props[SougouClient])

  def receive = {
    case searchTask:AggregateSearchTask =>
      aggregateSearchTask = searchTask
      println("Get AggregateSearchTask in AggregateSearchEngineActor.")
      startSearchServices(searchTask.excavatorInfo.key)

    case BaiduItems(items) =>
      baiduItems = items
      checkComplete(sender())

    case BingItems(items) =>
      bingItems = items
      checkComplete(sender())

    case QihooItems(items) =>
      qihooItems = items
      checkComplete(sender())

    case SmItems(items) =>
      smItems = items
      checkComplete(sender())

    case SougouItems(items) =>
      sougouItems = items
      checkComplete(sender())

    case Error(errMessage) =>
      errList = errList + errMessage
      checkComplete(sender())
  }

  private def startSearchServices(key:String) = {
    baiduService  ! StartSearchEngineWithKey(key)
    bingService   ! StartSearchEngineWithKey(key)
    qihooService  ! StartSearchEngineWithKey(key)
    smService     ! StartSearchEngineWithKey(key)
    sougouService ! StartSearchEngineWithKey(key)
  }

  private def checkComplete(sender:ActorRef) ={
    if((baiduItems!=null || errList.contains("baidu"))
      && (bingItems!=null || errList.contains("bing"))
      && (qihooItems!=null || errList.contains("qihoo"))
      && (smItems!=null || errList.contains("sm"))
      && (sougouItems!=null || errList.contains("sougou"))
    ){
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

      context.parent ! AggregateSearchResult(aggregateSearchTask, urlMapTitleItems)
    }
  }
}
