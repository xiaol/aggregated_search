package nlps

// Created by ZG on 15/9/9.
//

import akka.event.Logging
import java.util.regex.Pattern
import akka.actor.{ActorRef, Actor}
import cores.RestMessage
import spray.json.DefaultJsonProtocol
import scala.collection.mutable.ArrayBuffer
import org.ansj.app.keyword.KeyWordComputer
import org.ansj.util.MyStaticValue

import KeywordsExtractorActor._

class KeywordsExtractorActor extends Actor{

  implicit val system = context.system
  val log = Logging(context.system, this)

  val baseFilter = List("今天","明天","后天","中国","全国","美国","小时","原谅","标题")
  MyStaticValue.userLibrary = "library/stopwords.dic"
  MyStaticValue.ambiguityLibrary = "library/ambiguity.dic"
  val kwc = new KeyWordComputer(10)
  log.error("Init keywords extractor server...")

  def receive = {

    case ExtractKeywords(sentence) =>
      val sen = sentence.replaceAll("[\\pP\\pS]", "")
      val enKeywords = extractKeywordsEn(sen)
      val keywordsList = kwc.computeArticleTfidf(sentence, "").toArray.toList.map(key => key.toString.split("/")(0))
      sender ! Keywords(enKeywords ::: (for(key <- keywordsList if !baseFilter.contains(key)) yield key))
  }
}

object KeywordsExtractorActor{

  val enWordPattern = Pattern.compile("\\w+")

  val brackets = List(
    Pattern.compile("<[\\s\\S]*?>"),
    Pattern.compile("《[\\s\\S]*?》"),
    Pattern.compile("\\([\\s\\S]*?\\)"),
    Pattern.compile("（[\\s\\S]*?）"),
    Pattern.compile("[0-9]+")
  )

  def removeBrackets(sen:String):String = {
    var senRest = sen
    brackets.foreach(bracket =>
      senRest = bracket.matcher(senRest).replaceAll(""))
    senRest
  }

  def extractKeywordsEn(sentence:String):List[String] = {
    val wordsMathed = enWordPattern.matcher(sentence)
    val words:ArrayBuffer[String] = ArrayBuffer()
    while(wordsMathed.find())
      words += wordsMathed.group()
    words.filter(_.length>2).toList
  }
}

case class ExtractKeywords(sentence:String) extends RestMessage
case class Keywords(keywords:List[String]) extends RestMessage

object KeywordsJsonProtocol extends DefaultJsonProtocol{
  implicit val keywordsFormat = jsonFormat1(Keywords)
}

class KeywordsExtractorActorServer(keywordsExtractorClient:ActorRef) extends Actor{
  def receive = {
    case extractKeywords:ExtractKeywords => keywordsExtractorClient ! extractKeywords
    case keywords:Keywords => context.parent ! keywords
    case _ =>
  }
}