package com.search.core

import akka.actor.{Actor, ActorRef}
import com.search._

// Created by ZG on 15/7/29.
//

class ExcavatorEngineActor(extractService:ActorRef, cacheService:ActorRef) extends Actor{

  def receive = {
    case excavatorInfo:ExcavatorInfo =>
      if(excavatorInfo.key.isEmpty && excavatorInfo.url.isEmpty){
        context.parent ! Validation("NotEnoughParamError")
      }
      else{
        cacheService ! excavatorInfo
      }

    case errMessage:Validation => context.parent ! errMessage

    case responseMessage:ResponseMessage => context.parent ! responseMessage

    case ExcavatorWithUrlOrKey(excavatorInfo) =>
      println("Extract with url or title")
      extractService ! ExcavatorWithUrlOrKey(excavatorInfo)

    case ExcavatorWithKey(excavatorInfo) =>
      println("Extract with title")
      extractService ! ExcavatorWithKey(excavatorInfo)

    case _ =>
  }
}
