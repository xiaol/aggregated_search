package com.search.clients.media

import com.search._
import akka.actor.{Actor, ActorRef}
import com.search.clients.tools.{Extractor, Agents}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import spray.client.pipelining._

import scala.util.{Failure, Success}

import com.search.clients.media.ShenmaClient._

// Created by ZG on 15/7/20.
//

class ShenmaClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractShenmaByUrl(key) =>
      process(key, sender())
  }

  def process(url: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.pc)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(url)
      // html
    }
    responseFuture onComplete {
      case Success(response) =>
        println("ShenmaClient")
        val newscontent = extractor(url, response.entity.asString)
        if (newscontent.content.nonEmpty && !newscontent.title.isEmpty
          && !newscontent.source.isEmpty && !newscontent.updateTime.isEmpty)
          sender ! ExtractResult("shenma", newscontent)

      case Failure(error) => sender ! Error("None")
    }
  }

  def extractor(url: String, html:String) = {

    val doc = Jsoup.parse(html)

    var title:String = ""
    val tags:String = ""
    var source:String = ""
    val sourceUrl:String = ""
    val author:String = ""
    var updateTime:String = ""
    var imageNum:Int = 0
    var content:List[ContentBlock] = List()

    val titleNode = Extractor.getMentBySels(doc, List("h2[class=conttitle]"))
    if (titleNode!=null){
      title = titleNode.text()
    }

    val sourceNode = Extractor.getMentBySels(doc, List("span[class=sourceName]"))
    if (sourceNode!=null){
      source = sourceNode.text()
    }

    val updateTimeNode = Extractor.getMentBySels(doc, List("span[class=date-content]"))
    if (updateTimeNode!=null){
      updateTime = updateTimeNode.text()
    }

    val contentNode = Extractor.getMentBySels(doc, List("div[class=text conttext]"))
    if (contentNode!=null){
//      println("contentNode = " + contentNode.text())
      var node:Element = null
      var img:Element = null
      var txt:Element = null
      for(child <- contentNode.children().toArray){
        node = Jsoup.parse(child.toString)

        img = node.select("img").first()
        if(img!=null){
          content = content :+ ImageBlock(img.attr("data-src"))
          imageNum += 1
          if(!node.text().isEmpty)
            content = content :+ ImageInfoBlock(node.text())
        } else{
          txt = node.select("p").first()
          if(txt!=null && !txt.text().isEmpty)
            content = content :+ TextBlock(txt.text())
        }
      }
    }
    NewsContent(url, title, tags, source, sourceUrl, author, updateTime, imageNum, content)
  }
}

object ShenmaClient{
  case class ExtractShenmaByUrl(key: String)
  case class ShenmaResult(result: String)
}