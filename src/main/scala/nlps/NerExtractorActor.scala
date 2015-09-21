package nlps

// Created by ZG on 15/9/10.
//

import akka.event.Logging
import akka.actor.{ActorRef, Actor}
import cores.RestMessage
import spray.json.DefaultJsonProtocol
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.ansj.splitWord.analysis.NlpAnalysis
import java.util.regex.{Matcher, Pattern}

import NerExtractorActor._

class NerExtractorActor extends Actor{

  implicit val system = context.system
  val log = Logging(context.system, this)

  val basePath = "src/main/resources/ner/"
  // val basePath = "/work/pro/src/main/resources/ner/"

  val serializedClassifier = basePath + "classifiers/chinese.misc.distsim.crf.ser.gz"
  val ner = CRFClassifier.getClassifierNoExceptions(serializedClassifier)
  log.error("Init ners extractor server...")

  def receive = {
    case ExtractNers(sentense) =>
      sender ! format(ner.classifyWithInlineXML(NlpAnalysis.parse(sentense)
        .toArray.toList.map(_.toString.split("/").head).filter(_.nonEmpty).mkString(" ")))
  }
}

object NerExtractorActor{

  val patternTag = Pattern.compile("<[^<>]+?>")
  val patternGPE = Pattern.compile("<GPE>.*?</GPE>")
  val patternMISC = Pattern.compile("<MISC>.*?</MISC>")
  val patternORG = Pattern.compile("<ORG>.*?</ORG>")
  val patternPERSON = Pattern.compile("<PERSON>.*?</PERSON>")
  val patternLOC = Pattern.compile("<LOC>.*?</LOC>")

  val baseFilter = List("今天","明天","后天","中国","全国","美国","小时","原谅","标题")

  def extract(matcher: Matcher) = {
    var ners = scala.collection.mutable.Set[String]()
    while(matcher.find())
      ners += patternTag.matcher(matcher.group()).replaceAll("")
    ners.toList
  }

  def format(nerRes:String) = {
    val GPEList = for(x <- extract(patternGPE.matcher(nerRes)) if !baseFilter.contains(x) && x.length>2) yield x
    val TimeList = for{x <- extract(patternMISC.matcher(nerRes)) if !baseFilter.contains(x) && x.length>2 && {
      {if(x.contains("\u5e74")) 1 else 0} +
        {if(x.contains("\u6708")) 1 else 0} +
        {if(x.contains("\u65e5")) 1 else 0}}>=2} yield x
    val ORGList = extract(patternORG.matcher(nerRes))
    val PERSONList = extract(patternPERSON.matcher(nerRes))
    val LOCList = extract(patternLOC.matcher(nerRes))

    Ners(TimeList, GPEList, LOCList, PERSONList, ORGList)
  }
}

case class ExtractNers(sentence:String) extends RestMessage
case class Ners(time:List[String], gpe:List[String], loc:List[String],
                person:List[String], org:List[String]) extends RestMessage

object NerJsonProtocol extends DefaultJsonProtocol{
  implicit val nerFormat = jsonFormat5(Ners)
}

class NerExtractorActorServer(nerExtractorClient:ActorRef) extends Actor{
  def receive = {
    case extractNers:ExtractNers => nerExtractorClient ! extractNers
    case ners:Ners => context.parent ! ners
    case _ =>
  }
}
