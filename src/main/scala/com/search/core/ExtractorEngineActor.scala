package com.search.core

// Created by ZG on 15/8/3.
//

import akka.actor.{Props, Actor}

import com.search.clients.media._
import com.search._
import com.search.clients.redis.RedisClients
import com.search.clients.redis.RedisClients._
import com.search.clients.tools.DateUtils
import com.search.routing.PerRequestCreator
import reactivemongo.bson.{BSON, BSONObjectID}

//case class AutomaticExtractorResult(info: ExcavatorInfo, html:String)

class ExtractorEngineActor extends Actor with PerRequestCreator{

  // redis server
  val redisServer = context.actorOf(Props[RedisClients])

  def receive = {

    case ExcavatorWithKey(excavatorInfo) =>
      println("Start Aggregate Search with title")
      context.actorOf(Props[AggregateSearchEngineActor]) ! AggregateSearchTask(content=false, excavatorInfo)

    case AggregateSearchResult(searchTask, searchItems) =>
      if(!searchTask.content){
        println("This search start with ExcavatorWithKey, to get content by TemplateExtract")
        context.actorOf(Props[TemplateExtractorClient]) ! ExtractWithSearchItems(searchTask.excavatorInfo, searchItems)
        updateStatus(searchTask.excavatorInfo, searchItems)
      }
      else{
        updateStatus(searchTask.excavatorInfo, searchItems)           // Done
        checkTaskStatus(searchTask.excavatorInfo)
      }

    case TemplateExtractResult(excavatorInfo, extractResult) =>
      updateStatus(excavatorInfo, extractResult)                      // Done
      checkTaskStatus(excavatorInfo)

    case ExcavatorWithUrlOrKey(excavatorInfo) =>
      println("Start Automatic Extractor!!!")
      context.actorOf(Props[AutomaticExtractorClient]) ! excavatorInfo

    case AutomaticExtractResult(excavatorInfo, extractResult) =>
      println("Get AutomaticExtractorResult!")
      context.actorOf(Props[AggregateSearchEngineActor]) ! AggregateSearchTask(content=true, excavatorInfo)
      updateStatus(excavatorInfo, extractResult)

    case _ =>
  }

  private def checkTaskStatus(excavatorInfo:ExcavatorInfo) = {
    println("checkTaskStatus")

    val statuTable = if(excavatorInfo.url.nonEmpty)
      s"${excavatorInfo.uid}:url_${excavatorInfo.url}" else s"${excavatorInfo.uid}:key_${excavatorInfo.key}"

    val excavatorTask = hGetAll(statuTable).get
    if(excavatorTask.get("content").nonEmpty && excavatorTask.get("aggreItems").nonEmpty){
      if(excavatorTask.get("status").get == "0"){ println("This task already updated to mongo.")}
      else{
        println("content and aggresearch is already!!")
        println("update to mongoDB!")

        import com.search.db._
        import com.search.db.NewsItem._
        var items:List[Line] = List()
        for(item <- excavatorTask.get("content").get.split("&&")){
          items = items :+ Line(items.length.toString, item.split("=>").head, item.split("=>")(1))
        }
        var aggreItems:List[UlrMapTitle] = List()
        for(item <-  excavatorTask.get("aggreItems").get.split("&&")){
          aggreItems = aggreItems :+ UlrMapTitle(item.split("=>").head, item.split("=>")(1))
        }
        val insertId = BSONObjectID.generate.stringify
        val document = Item(
          insertId,
          excavatorTask.getOrElse("user_id", ""),
          excavatorTask.getOrElse("search_url", ""),
          excavatorTask.getOrElse("search_key", ""),
          excavatorTask.getOrElse("alid", ""),
          "",               // cont.url
          excavatorTask.getOrElse("title", ""),
          "",               // cont.tags
          "",               // cont.source
          "",               // cont.sourceUrl
          "",               // cont.author
          "",               // cont.updateTime
          DateUtils.getCurrentDate,
          "",               // cont.imageNum.toString
          Lines(items),
          AggreItems(aggreItems)
        )
        ReactiveMongo.insertMongo("testForReactiveMongoV1", BSON.write(document))

        println("change status to 0, add complete time. add inserted ID of mongo.")
        hmSet(
          HashTable(statuTable, "status", "0") ::
            HashTable(statuTable, "inserteId", insertId) ::
            HashTable(statuTable, "completeTime", DateUtils.getCurrentDate) ::
            Nil)
      }
    }
  }

  private def updateStatus(excavatorInfo:ExcavatorInfo, items:List[UrlMapTitleItem]) = {

    println("Update redis by searchItems!")
    val statuTable = if(excavatorInfo.url.nonEmpty)
      s"${excavatorInfo.uid}:url_${excavatorInfo.url}" else s"${excavatorInfo.uid}:key_${excavatorInfo.key}"
    println("statuTable:", statuTable)

    val aggreItems = for(item <- items) yield s"${item.url}=>${item.title}"
    hSet(statuTable, "aggreItems", aggreItems.mkString("&&"))
  }

  private def updateStatus(excavatorInfo:ExcavatorInfo, extractResult:ExtractResult) = {

    val statuTable = if(excavatorInfo.url.nonEmpty)
      s"${excavatorInfo.uid}:url_${excavatorInfo.url}" else s"${excavatorInfo.uid}:key_${excavatorInfo.key}"
    println("statuTable:", statuTable)

    val contentFlag = hmGet(statuTable, "content").get

    if(contentFlag.isEmpty){
      println("Update redis by extractResult!")
      val title = extractResult.newsContent.title
      val content = for(block <- extractResult.newsContent.content) yield {
        block match{
          case textBlock:TextBlock =>  s"txt=>${textBlock.text}"
          case imageBlock:ImageBlock =>  s"img=>${imageBlock.src}"
          case imageInfoBlock:ImageInfoBlock =>  s"img_info=>${imageInfoBlock.text}"
        }
      }
      hmSet(
          HashTable(statuTable, "title", title) ::
          HashTable(statuTable, "content", content.mkString("&&")) ::
          Nil)
    }
    else{
      println("Drop content!!!")
    }
  }
}

