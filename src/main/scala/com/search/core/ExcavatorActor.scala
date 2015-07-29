package com.search.core

import akka.actor.{PoisonPill, Props, Actor}
import com.search.clients.media.NeteasyClient.ExtractNeteasyByUrl
import com.search.clients.media.ShenmaClient.ExtractShenmaByUrl
import com.search.clients.media.SinaClient.ExtractSinayByUrl
import com.search.clients.media.SohuClient.ExtractSohuByUrl
import com.search.clients.media.TencentClient.ExtractTencentByUrl
import com.search.clients.media._
import com.search.clients.tools.Redis
import com.search.db.{UlrMapTitle, ReactiveMongo}
import com.search.routing.PerRequestCreator
import reactivemongo.bson.{BSONObjectID, BSON, BSONArray, BSONDocument}

// Created by ZG on 15/7/24.
//

import com.search._

class ExcavatorActor extends Actor with PerRequestCreator{

  implicit val system = context.system

  var userId:String = ""
  var searchKey:String = ""
  var searchResultItems: UrlMapTitleItems = _
  var insertId:String = ""

  val neteasyService = context.actorOf(Props[NeteasyClient])
  val sinaService = context.actorOf(Props[SinaClient])
  val sohuService = context.actorOf(Props[SohuClient])
  val tencentService = context.actorOf(Props[TencentClient])
  val shenmaService = context.actorOf(Props[ShenmaClient])

  def receive = {

    case ExtractResult(mediaName, newsContent) =>
      if(getStatusFromRedis!="0"){
//        println(newsContent)
//        println(searchResultItems)

        val doc = fortatNewsItem(newsContent)
//        println(BSONDocument.pretty(doc))
        ReactiveMongo.insertMongo(doc)
        setStatusToRedis()
      }

    case SearchItemsWithInfo(uid, key, urlMapTitleItems) =>
      userId = uid
      searchKey = key
      searchResultItems = UrlMapTitleItems(urlMapTitleItems)
      for(item <- urlMapTitleItems){
        item match {
          case UrlMapTitleItem(url, title) if item.url.contains("163.com/") => neteasyService ! ExtractNeteasyByUrl(url)
          case UrlMapTitleItem(url, title) if item.url.contains("sohu.com/") => sohuService ! ExtractSohuByUrl(url)
          case UrlMapTitleItem(url, title) if item.url.contains("sina.com") => sinaService ! ExtractSinayByUrl(url)
          case UrlMapTitleItem(url, title) if item.url.contains("qq.com/") => tencentService ! ExtractTencentByUrl(url)
          case UrlMapTitleItem(url, title) if item.url.contains("zzd.sm.cn/") => shenmaService ! ExtractShenmaByUrl(url)
          case _ =>
        }
      }

    case _ =>
  }

  def fortatNewsItem(cont:NewsContent) = {
    import com.search.db._
    import com.search.db.NewsItem._

    var items:List[Line] = List()
    for(item <- cont.content){
      item match{
        case TextBlock(text) => items = items :+ Line(items.length.toString, "txt", text)
        case ImageBlock(src) => items = items :+ Line(items.length.toString, "img", src)
        case ImageInfoBlock(text) => items = items :+ Line(items.length.toString, "img_info", text)
      }
    }
    var aggreItems:List[UlrMapTitle] = List()
    for(item <- searchResultItems.items){
      aggreItems = aggreItems :+ UlrMapTitle(item.url, item.title)
    }


    val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    insertId = BSONObjectID.generate.stringify
    val document = Item(
      insertId,
      userId,
      searchKey,
      cont.url,
      cont.title,
      cont.tags,
      cont.source,
      cont.sourceUrl,
      cont.author,
      cont.updateTime,
      format.format(new java.util.Date()),
      cont.imageNum.toString,
      Lines(items),
      AggreItems(aggreItems)
    )
    BSON.write(document)
  }

  def setStatusToRedis() = {
    Redis.hmSet(List(userId, searchKey).mkString(":"), Map("status"->"0", "insertId"->insertId))
    println(insertId)
  }

  def getStatusFromRedis = {
    val item = Redis.hmGet(List(userId, searchKey).mkString(":"), "status")
    if(item!=null){
      item.get.get("status").get
    } else "1"
  }

  private def killYourself() = self ! PoisonPill
}
