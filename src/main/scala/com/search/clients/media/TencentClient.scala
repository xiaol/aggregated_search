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

import com.search.clients.media.TencentClient._

class TencentClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractTencentByUrl(key) =>
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
        println("TencentClient")
        val newscontent = extractor(url, response.entity.asString)
        if (newscontent.content.nonEmpty && !newscontent.title.isEmpty
          && !newscontent.source.isEmpty && !newscontent.updateTime.isEmpty)
          sender ! ExtractResult("tencent", newscontent)

      case Failure(error) => sender ! Error("None")
    }
  }

  def extractor(url:String, html:String) = {

    val doc = Jsoup.parse(html)

    var title:String = ""
    val tags:String = ""
    var source:String = ""
    val sourceUrl:String = ""
    val author:String = ""
    var updateTime:String = ""
    var imageNum:Int = 0
    var content:List[ContentBlock] = List()

    val titleNode = Extractor.getMentBySels(doc, List("h1[class=title]", "title"))
    if (titleNode!=null)
      title = titleNode.text().split("_")(0)

    val sourceNode = Extractor.getMentBySels(doc, List("span[class=author]"))
    if (sourceNode!=null)
      source = sourceNode.text()

    val updateTimeNode = Extractor.getMentBySels(doc, List("span[class=time]"))
    if (updateTimeNode!=null)
      updateTime = updateTimeNode.text()

    val contentNode = Extractor.getMentBySels(doc, List("div[class=content fontsmall]"))
    if (contentNode!=null){
//      println("contentNode = " + contentNode.text())
      var node:Element = null
      var img:Element = null
      var txt:Element = null
      var dropTag:Element = null

      dropTag =contentNode.select("div[class=video split]").first()
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
            txt.text().split("\\u0020\\u3000\\u3000").foreach(x =>
              content = content :+ TextBlock(x.replaceAll("\\u3000\\u3000", ""))
            )
        }
      }
    }
    NewsContent(url, title, tags, source, sourceUrl, author, updateTime, imageNum, content)
  }
}

object TencentClient{
  case class ExtractTencentByUrl(key: String)
  case class TencentResult(result: String)
}
