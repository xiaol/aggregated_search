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

class SinaClient extends Actor{

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
          && !newscontent.updateTime.isEmpty)
          sender ! MediaResult("SinaClient", newscontent)
        else sender ! MediaResult("SinaClient", NewsContent("","","","","","","",0,List[ContentBlock]()))

      case Failure(error) => sender ! MediaResult("SinaClient", NewsContent("","","","","","","",0,List[ContentBlock]()))
    }
  }

  def extractor(url:String, html:String) = {

    val doc = Jsoup.parse(html)

    val tags:Option[String] = None
    val source:Option[String] = None
    val sourceUrl:Option[String] = None
    val author:Option[String] = None

    var title:Option[String] = None
    var updateTime:Option[String] = None
    var imageNum:Int = 0
    val content:ArrayBuffer[ContentBlock] = ArrayBuffer()

    val titleNode = Extractor.getMentBySels(doc, List("title"))
    if (titleNode!=null) title = Some(titleNode.text().split("_")(0))

    val updateTimeNode = Extractor.getMentBySels(doc, List("span[class=source]"))
    if (updateTimeNode!=null) updateTime = Some(updateTimeNode.text().split(" ")(0))

    val contentNode = Extractor.getMentBySels(doc, List("div[id=j_articleContent]"))
    if (contentNode!=null){

      List("div[class=article-module video]",
        "div[class=article-module title]",
        "div[class=load-more]",
        "div[class=article-module hot]",
        "article[class=M_attitude ]",
        "div[class=mark-sign]",
        "div[id=wx_pic]"
      ).foreach{ dropXpath =>
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
        else{
          val txt = node.select("p").first()
          if (txt != null && !txt.text().isEmpty) content += TextBlock(txt.text())
        }

      }
    }

    NewsContent(url, title.getOrElse(""), tags.getOrElse(""), source.getOrElse(""),
      sourceUrl.getOrElse(""), author.getOrElse(""), updateTime.getOrElse(""), imageNum, content.toList)
  }
}