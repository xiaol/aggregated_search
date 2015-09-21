package clients.community

// Created by ZG on 15/8/21.
//

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}
import utils.Agents
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets

case class ExtractZhihus(key:String)
case class Zhihu(url:String, title:String, user:String)
case class Zhihus(zhihus:List[Zhihu])

class ZhihuClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractZhihus(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://www.zhihu.com/search?q=${java.net.URLEncoder.encode(key,"UTF-8")}&type=question")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(key,response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! Zhihus(List[Zhihu]())
    }
  }

  def extractor(key:String, html:String) ={
    val zhihuList:ArrayBuffer[Zhihu] = ArrayBuffer()

    val doc = Jsoup.parse(html, "UTF-8")

    val searchResultLists = doc.select("li[class=item clearfix]")
    if(searchResultLists!=null){
      searchResultLists.toArray.foreach { node =>
        val block = Jsoup.parse(node.toString)

        val titleNode = block.select("div[class=title]")
        val authorNode = block.select("a[class=author")
        if(titleNode!=null && authorNode!=null){
          val title = titleNode.text()
          val url = titleNode.select("a").attr("href")
          val user = authorNode.text()
          zhihuList.append(Zhihu(title, if(url.startsWith("http")) url else "http://www.zhihu.com"+url, user))
        }
      }
    }
    Zhihus(zhihuList.toList)
  }

}
