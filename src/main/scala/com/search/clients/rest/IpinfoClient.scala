package com.search.clients.rest

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import spray.http._
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._

import scala.util.{Failure, Success}

import com.search.Error
import com.search.clients.rest.IpinfoClient._

import spray.json._
import spray.json.DefaultJsonProtocol

case class Data(ip:String, country:String, area:String, region:String, county:String, isp:String, city:String,
                country_id:String, area_id:String, region_id:String, city_id:String, county_id:String, isp_id:String)
case class Info(code:Int, data:Data)

object IpinfoJsonProtocol extends DefaultJsonProtocol{
  implicit val dataformat = jsonFormat13(Data.apply)
  implicit val infoFormat = jsonFormat2(Info.apply)
}


class IpinfoClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case GetInfoByIp(ip) =>
      process(ip, sender())
  }

  def process(key: String, sender: ActorRef) = {
    import IpinfoJsonProtocol._
    //    val pipeline = sendReceive
    val pipeline = sendReceive

    val responseFuture = pipeline {
      // the key must be urlencode
      // the user-agent must be mobile
      Get(s"http://ip.taobao.com/service/getIpInfo.php?ip=$key")
      // return json
    }
    responseFuture onComplete {
      case Success(response) =>
        println(response.entity.asString.parseJson.convertTo[Info])
        sender ! IpinfoResult(response.entity.toString)

      case Failure(error) => sender ! IpinfoResult("None")
    }
  }
}

object IpinfoClient {
  case class GetInfoByIp(ip: String)
  case class IpinfoResult(result: String)
}



//case class Timezone(status: String, timeZoneId: String, timeZoneName: String)
//case class GoogleTimezoneApiResult[T](status: String, timeZoneId: String, timeZoneName: String)
//
//object TimezoneJsonProtocol extends DefaultJsonProtocol {
//  implicit val timezoneFormat = jsonFormat3(Timezone)
//  implicit def googleTimezoneApiResultFormat[T :JsonFormat] = jsonFormat3(GoogleTimezoneApiResult.apply[T])
//}