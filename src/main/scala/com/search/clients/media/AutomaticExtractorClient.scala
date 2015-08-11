package com.search.clients.media

import akka.actor.{ActorRef, Actor}
import com.search._
import com.search.clients.tools.Agents
import de.l3s.boilerpipe.extractors.ChineseArticleExtractor
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets

import scala.util.{Failure, Success}

// Created by ZG on 15/8/4.
// 

class AutomaticExtractorClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  var excavatorInfo:ExcavatorInfo = _

  def receive = {
    case info:ExcavatorInfo =>
      excavatorInfo = info
      process(sender())
  }

  def process(sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(excavatorInfo.url)
      // html
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) =>
    }
  }

  def extractor(html:String) = {
    println("Extract in automatic extractor!")

    val title:String = Jsoup.parse(html).title.split("-")(0).split(" - ")(0)
    println("Get title = " + title)
    if(excavatorInfo.key.isEmpty) {
      excavatorInfo = ExcavatorInfo(excavatorInfo.uid, excavatorInfo.album, excavatorInfo.url, title)
    }

    val tags:String = ""
    val source:String = ""
    val sourceUrl:String = ""
    val author:String = ""
    val updateTime:String = ""
    val imageNum:Int = 0
    var content:List[ContentBlock] = List()

    val ae = ChineseArticleExtractor.INSTANCE
    val doc = ae.getText(html)
    if(doc.trim.length != 0){
      println("Get content:")
      println(doc)
      doc.trim.split("\n").toList.foreach(line =>
        content = content :+ TextBlock(line)
      )
      AutomaticExtractResult(excavatorInfo, ExtractResult("AutomaticExtractor",
        NewsContent(excavatorInfo.url,title,tags,source,sourceUrl,author,updateTime,imageNum,content)))
    }
    else{
      println("No content get, sender to extract by key!")
      ExcavatorWithKey(excavatorInfo)
    }
  }
}
