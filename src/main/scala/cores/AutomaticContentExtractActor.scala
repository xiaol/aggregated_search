package cores

// Created by ZG on 15/9/11.
//

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import clients.media.{NewsContent, MediaResult, TextBlock, ContentBlock}
import utils.Agents
import de.l3s.boilerpipe.extractors.ChineseArticleExtractor
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

class AutomaticContentExtractActor extends Actor{

  implicit val system = context.system
  import system.dispatcher

  val log = Logging(context.system, this)

  def receive = {
    case ExtractContentByAutomatic(url) =>
      log.error(s"AutomaticContentExtractActor with url: $url")
      process(url, sender())
    case _ =>
  }

  def process(url:String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(url)
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(url, response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) =>
        sender ! MediaResult("AutomaticExtractor", NewsContent("","","","","","","",0,List[ContentBlock]()))
    }
  }

  def extractor(url:String, html:String) = {
    val title:String = Jsoup.parse(html).title.split("-")(0).split(" - ")(0)

    val tags:Option[String] = None
    val source:Option[String] = None
    val sourceUrl:Option[String] = None
    val author:Option[String] = None
    val updateTime:Option[String] = None
    val imageNum:Int = 0
    var content:ArrayBuffer[ContentBlock] = ArrayBuffer()

    val ae = ChineseArticleExtractor.INSTANCE
    val doc = ae.getText(html)
    if(doc.trim.length != 0){
      println("Get content:")
      println(doc)
      doc.trim.split("\n").toList.foreach(line =>
        content += TextBlock(line)
      )
      MediaResult("AutomaticExtractor",
        NewsContent(url,title,tags.getOrElse(""),source.getOrElse(""),sourceUrl.getOrElse(""),
          author.getOrElse(""),updateTime.getOrElse(""),imageNum,content.toList))
    }
    else{
      MediaResult("AutomaticExtractor", NewsContent(url,title,"","","","","",0,List[ContentBlock]()))
    }
  }

}
