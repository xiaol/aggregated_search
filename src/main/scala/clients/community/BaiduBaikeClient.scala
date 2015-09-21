package clients.community

// Created by ZG on 15/8/21.
//

import akka.actor.{Actor, ActorRef}
import utils.{Extractor, Agents}
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.HttpCharsets
import scala.util.{Failure, Success}

case class ExtractBaiduBaike(key:String)
case class BaiduBaike(title:String, url:String, abs:String)

class BaiduBaikeClient extends Actor{

  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case ExtractBaiduBaike(key) => process(key, sender())
  }

  def process(key: String, sender: ActorRef) = {
    val pipeline =(
      addHeader("User-agent", Agents.mobile)
        ~> sendReceive
      )
    val responseFuture = pipeline {
      Get(s"http://baike.baidu.com/search?word=${java.net.URLEncoder.encode(key,"UTF-8")}&pn=0&rn=0&enc=utf8")
    }
    responseFuture onComplete {
      case Success(response) =>
        sender ! extractor(key,response.entity.asString(HttpCharsets.`UTF-8`))

      case Failure(error) => sender ! BaiduBaike("","","")
    }
  }

  def extractor(key:String, html:String) ={
    var title:String = ""
    var url:String = ""
    var abstr:String = ""

    val doc = Jsoup.parse(html, "UTF-8")

    val searchResultLists = Extractor.getMentBySels(doc, List("dl[class=search-list]"))
    if(searchResultLists!=null){
      val searchResult = searchResultLists.select("dd").first()
      if(searchResult!=null){
        val a = searchResult.getElementsByTag("a")
        val p = searchResult.getElementsByTag("p")
        if(a!=null && p!=null){
          title = a.text()
          url = a.attr("href")
          abstr = p.text()
        }
      }
    }
    BaiduBaike(title, url, abstr)
  }
}
