package clients.media

// Created by ZG on 15/7/20.
//

import akka.actor.{Actor, ActorRef}
import spray.http.HttpCharsets
import utils.{Extractor, Agents}
import org.jsoup.Jsoup
import spray.client.pipelining._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

class SohuClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractMedia(url) => process(url, sender())
  }

  def process(url: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(url)
    }
    responseFuture onComplete {
      case Success(response) =>
        val newscontent = extractor(url, response.entity.asString(HttpCharsets.`UTF-8`))
        if (newscontent.content.nonEmpty && !newscontent.title.isEmpty
          && !newscontent.source.isEmpty && !newscontent.updateTime.isEmpty)
          sender ! MediaResult("SohuClient", newscontent)
        else
          sender ! MediaResult("SohuClient", NewsContent("","","","","","","",0,List[ContentBlock]()))

      case Failure(error) =>
        sender ! MediaResult("SohuClient", NewsContent("","","","","","","",0,List[ContentBlock]()))
    }
  }

  def extractor(url:String, html:String) = {

    val doc = Jsoup.parse(html)

    val tags:Option[String] = None
    val sourceUrl:Option[String] = None
    val author:Option[String] = None

    var title:Option[String] = None
    var source:Option[String] = None
    var updateTime:Option[String] = None
    var imageNum:Int = 0
    val content:ArrayBuffer[ContentBlock] = ArrayBuffer()

    val titleNode = Extractor.getMentBySels(doc, List("h1[class=finTit]"))
    if (titleNode!=null) title = Some(titleNode.text().split("_")(0))

    val sourceNode = Extractor.getMentBySels(doc, List("b[class=ori]"))
    if (sourceNode!=null) source = Some(sourceNode.text())

    val updateTimeNode = Extractor.getMentBySels(doc, List("div[class=inf]"))
    if (updateTimeNode!=null) updateTime = Some(updateTimeNode.text().split(" ")(0))

    val contentNode = Extractor.getMentBySels(doc, List("div[class=finCnt]"))
    if (contentNode!=null){

      List("div[class=toShare clearfix]",
        "div[class=finVideo]").foreach{ dropXpath =>
        val dropTag =contentNode.select(dropXpath).first()
        if(dropTag!=null) dropTag.remove()
      }

      contentNode.children().toArray.foreach{ child =>

        val node = Jsoup.parse(child.toString)
        val img = node.select("img").first()
        if (img != null) {
          content += ImageBlock(img.attr("src"))
          imageNum += 1
          if (!node.text().isEmpty) content += ImageInfoBlock(node.text())
        }
        else {
          val txt = node.select("p").first()
          if (txt != null && !txt.text().isEmpty) content += TextBlock(txt.text())
        }
      }
    }

    NewsContent(url, title.getOrElse(""), tags.getOrElse(""), source.getOrElse(""),
      sourceUrl.getOrElse(""), author.getOrElse(""), updateTime.getOrElse(""), imageNum, content.toList)
  }
}