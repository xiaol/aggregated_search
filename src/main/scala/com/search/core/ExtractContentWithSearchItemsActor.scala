package com.search.core

// Created by ZG on 15/7/23.
//

import akka.actor.{ActorRef, Actor}
import com.search._
import com.search.clients.tools.Base64Utils

class ExtractContentWithSearchItemsActor(neteasyService:ActorRef, sinaService:ActorRef, sohuService:ActorRef,
                                         tencentService:ActorRef, shenmaService:ActorRef) extends Actor{

  def receive = {
    case SearchWithKey(key) =>
      startMediaServices(key)

    case extractResult:ExtractResult =>
      context.parent ! extractResult
  }

  private def startMediaServices(segItems:String) = {
    for(item <- Base64Utils.DecodeBase64(segItems).split("&&")){
      item match {
        case url:String if url.split("=>").head.contains("163.com/") => neteasyService   ! StartExtractMediaWithUrl(url)
        case url:String if url.split("=>").head.contains("sohu.com/") => sohuService     ! StartExtractMediaWithUrl(url)
        case url:String if url.split("=>").head.contains("sina.com") => sinaService      ! StartExtractMediaWithUrl(url)
        case url:String if url.split("=>").head.contains("qq.com/") => tencentService    ! StartExtractMediaWithUrl(url)
        case url:String if url.split("=>").head.contains("zzd.sm.cn/") => shenmaService  ! StartExtractMediaWithUrl(url)
        case _ =>
      }
    }
  }
}
