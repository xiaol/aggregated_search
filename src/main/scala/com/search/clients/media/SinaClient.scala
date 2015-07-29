package com.search.clients.media

// Created by ZG on 15/7/20.
// 

import akka.actor.{Actor, ActorRef}
import com.search._
import com.search.clients.tools.{Extractor, Agents}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import spray.client.pipelining._

import scala.util.{Failure, Success}

import com.search.clients.media.SinaClient._

class SinaClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractSinayByUrl(key) =>
      process(key, sender())
  }

  def process(url: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(url)
      // html
    }
    responseFuture onComplete {
      case Success(response) =>
        println("SinaClient")
        val newscontent = extractor(url, response.entity.asString)
        if (newscontent.content.nonEmpty && !newscontent.title.isEmpty
          && !newscontent.updateTime.isEmpty)
          sender ! ExtractResult("sina", newscontent)

      case Failure(error) => sender ! Error("None")
    }
  }

  def extractor(url:String, html:String) = {

    val doc = Jsoup.parse(html)

    var title:String = ""
    val tags:String = ""
    val source:String = ""
    val sourceUrl:String = ""
    val author:String = ""
    var updateTime:String = ""
    var imageNum:Int = 0
    var content:List[ContentBlock] = List()

    val titleNode = Extractor.getMentBySels(doc, List("title"))
    if (titleNode!=null)
      title = titleNode.text().split("_")(0)

    val updateTimeNode = Extractor.getMentBySels(doc, List("span[class=source]"))
    if (updateTimeNode!=null)
      updateTime = updateTimeNode.text().split(" ")(0)

    val contentNode = Extractor.getMentBySels(doc, List("div[id=j_articleContent]"))
    if (contentNode!=null){
//      println("contentNode = " + contentNode.text())
      var node:Element = null
      var img:Element = null
      var txt:Element = null
      var dropTag:Element = null

      dropTag =contentNode.select("div[class=article-module video]").first()
      if(dropTag!=null) dropTag.remove()
      dropTag = contentNode.select("div[class=article-module title]").first()
      if(dropTag!=null) dropTag.remove()
      dropTag = contentNode.select("div[class=load-more]").first()
      if(dropTag!=null) dropTag.remove()
      dropTag = contentNode.select("div[class=article-module hot]").first()
      if(dropTag!=null) dropTag.remove()
      dropTag = contentNode.select("article[class=M_attitude ]").first()
      if(dropTag!=null) dropTag.remove()
      dropTag = contentNode.select("div[class=mark-sign]").first()
      if(dropTag!=null) dropTag.remove()

      for(child <- contentNode.children().toArray) {
        node = Jsoup.parse(child.toString)

        img = node.select("img").first()
        if (img != null) {
          content = content :+ ImageBlock(img.attr("src"))
          imageNum += 1
          if (!node.text().isEmpty)
            content = content :+ ImageInfoBlock(node.text())
        } else {
          txt = node.select("p").first()
          if (txt != null && !txt.text().isEmpty)
            content = content :+ TextBlock(txt.text())
        }
      }
    }
    NewsContent(url, title, tags, source, sourceUrl, author, updateTime, imageNum, content)
  }
}

object SinaClient{
  case class ExtractSinayByUrl(key: String)
  case class SinaResult(result: String)
}