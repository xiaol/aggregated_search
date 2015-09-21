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

class TencentClient extends Actor{

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
          sender ! MediaResult("TencentClient", newscontent)
        else
          sender ! MediaResult("TencentClient", NewsContent("","","","","","","",0,List[ContentBlock]()))

      case Failure(error) =>
        sender ! MediaResult("TencentClient", NewsContent("","","","","","","",0,List[ContentBlock]()))
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
    var content:ArrayBuffer[ContentBlock] = ArrayBuffer()

    val titleNode = Extractor.getMentBySels(doc, List("h1[class=title]", "h1[id=news-title]", "title"))
    if (titleNode!=null) title = Some(titleNode.text().split("_")(0))

    val sourceNode = Extractor.getMentBySels(doc, List("span[class=author]"))
    if (sourceNode!=null) source = Some(sourceNode.text())

    val updateTimeNode = Extractor.getMentBySels(doc, List("time[id=news-time]"))
    if (updateTimeNode!=null) updateTime = Some(updateTimeNode.text())

    val contentNode = Extractor.getMentBySels(doc, List("div[class=content fontsmall]","article[id=news-article]"))
    if (contentNode!=null){

      val dropTag =contentNode.select("div[class=video split]").first()
      if(dropTag!=null) dropTag.remove()

      contentNode.children().toArray.foreach{ child =>
        val node = Jsoup.parse(child.toString)
        val img = node.select("img").first()
        if (img != null) {
          content += ImageBlock(img.attr("src"))
          imageNum += 1
          if (!node.text().isEmpty) content += ImageInfoBlock(node.text())
        } else {
          val txt = node.select("p").first()
          if (txt != null && !txt.text().isEmpty)
            txt.text().split("\\u0020\\u3000\\u3000").foreach(x =>
              content += TextBlock(x.replaceAll("\\u3000\\u3000", ""))
            )
        }
      }
    }

    NewsContent(url, title.getOrElse(""), tags.getOrElse(""), source.getOrElse(""),
      sourceUrl.getOrElse(""), author.getOrElse(""), updateTime.getOrElse(""), imageNum, content.toList)
  }
}