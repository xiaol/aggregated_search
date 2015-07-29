package com.search.core

import akka.actor.{ActorRef, Actor}


import com.search._
import com.search.clients.media.NeteasyClient._
import com.search.clients.media.SinaClient._
import com.search.clients.media.SohuClient._
import com.search.clients.media.TencentClient._
import com.search.clients.rest.IpinfoClient._

// Created by ZG on 15/7/23.
// 

class ExtractContentWithUrlActor(sinaService:ActorRef, neteasyService:ActorRef,
                                 sohuService:ActorRef, tencentService:ActorRef) extends Actor{

  var neteasyInfo = ""
  var sinaInfo = ""
  var sohuInfo = ""
  var tencentInfo = ""

  def receive = {
    case ExtractWithUrl(key) => {
      println("in ExtractWithUrl")
      sinaService ! ExtractSinayByUrl(key)
      neteasyService ! ExtractNeteasyByUrl(key)
      sohuService ! ExtractSohuByUrl(key)
      tencentService ! ExtractTencentByUrl(key)

      context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {

    case SinaResult(result) => {
      sinaInfo = result
      replyIfReady()
    }
    case NeteasyResult(result) => {
      neteasyInfo = result
      replyIfReady()
    }
    case SohuResult(result) => {
      sohuInfo = result
      replyIfReady()
    }
    case TencentResult(result) => {
      tencentInfo = result
      replyIfReady()
    }
    case f: Validation => context.parent ! f
  }

  def replyIfReady() =
    println("in ExtractWithUrl")
    if(!neteasyInfo.isEmpty || !sinaInfo.isEmpty || !sohuInfo.isEmpty || !tencentInfo.isEmpty) {

      val results = neteasyInfo + "<--->" + sinaInfo + "<--->" + sohuInfo + "<--->" + tencentInfo
      //      val results = ipInfo
      context.parent ! TestResults(results)
    }
}
