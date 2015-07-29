package com.search.core

// Created by ZG on 15/7/20.
//

import akka.actor.{Props, ActorRef, Actor, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate

import com.search._
import com.search.clients.search._
import com.search.clients.search.BaiduClient.{SearchBaiduByKey, BaiduResult}
import com.search.clients.search.SougouClient.{SearchSougouByKey, SougouResult}
import com.search.clients.search.BingClient.{BingResult, SearchBingByKey}
import com.search.clients.search.QihooClient.{QihooResult, SearchQihooByKey}
import com.search.clients.search.SmClient.{SmResult, SearchSmByKey}
import com.search.clients.tools.Redis

class GetResultsWithkeysActor(baiduService: ActorRef, bingService: ActorRef, qihooService: ActorRef, smService:ActorRef,
                              sougouService: ActorRef, excavatorService:ActorRef) extends Actor {

  var userId:String = ""
  var searchKey:String = ""
  var errList:String = ""
  var ipInfo = ""
  var baiduItems:List[BaiduItem] = null
  var bingItems:List[BingItem] = null
  var qihooItems:List[QihooItem] = null
  var smItems:List[SmItem] = null
  var sougouItems:List[SougouItem] = null

  def receive = {
    case SearchWithKey(uid, key) =>
      userId = uid
      searchKey = key
      baiduService  ! SearchBaiduByKey(key)
      bingService   ! SearchBingByKey(key)
      qihooService  ! SearchQihooByKey(key)
      smService     ! SearchSmByKey(key)
      sougouService ! SearchSougouByKey(key)

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
    if(
      (baiduItems!=null || errList.contains("baidu"))
        && (bingItems!=null || errList.contains("bing"))
        && (qihooItems!=null || errList.contains("qihoo"))
        && (smItems!=null || errList.contains("sm"))
        && (sougouItems!=null || errList.contains("sougou"))
    ) {
      var urlSet:Set[String] = Set()
      var urlMapTitleItems:List[UrlMapTitleItem] = List()

      if(baiduItems!=null){
        for(item <- baiduItems) if(!urlSet.contains(item.url)) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(bingItems!=null){
        for(item <- bingItems) if(!urlSet.contains(item.url)) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(qihooItems!=null){
        for(item <- qihooItems) if(!urlSet.contains(item.url)) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(smItems!=null){
        for(item <- smItems) if(!urlSet.contains(item.url)) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
      if(sougouItems!=null){
        for(item <- sougouItems) if(!urlSet.contains(item.url)) {
          urlMapTitleItems = urlMapTitleItems :+ UrlMapTitleItem(item.url, item.title)
          urlSet += item.url
        }
      }
//      urlMapTitleItems.foreach(item => println(item.title, item.url))


      // return to complete
      val results = s"$userId:$searchKey"
      context.parent ! TestResults(results)

      // update the status to redis by "status=1"
      Redis.hmSet(List(userId, searchKey).mkString(":"), Map("status"->"1"))

      // send message to excavatorService to extract the content by Items
      excavatorService ! SearchItemsWithInfo(userId, searchKey, urlMapTitleItems)
    }
}
