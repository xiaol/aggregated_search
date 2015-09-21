package cores

// Created by ZG on 15/9/14.
//

import akka.event.Logging
import akka.actor.{Props, Actor, ActorRef}
import clients.community._
import clients.media._
import models.{Lines, Line, NewsItem}
import mongos.ReactiveMongo
import nlps.{Ners, Keywords, ExtractNers, ExtractKeywords}
import reactivemongo.bson.{BSON, BSONObjectID}
import redis.RedisClients
import redis.RedisClients.{HashTable,HashTableList}
import utils.DateUtils
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s._

class ExcavatorServiceActor(excavatorCacheServer:ActorRef, excavatorProccessorServer:ActorRef)
  extends Actor{

  val log = Logging(context.system, this)

  def receive = {
    case excavatorInfo:ExcavatorInfo =>
      if(excavatorInfo.key.isEmpty && excavatorInfo.url.isEmpty){
        log.error("Excavator error: NotEnoughParamError.")
        context.parent ! Validation("NotEnoughParamError")
      }
      else{
        log.debug("Check cache with excavatorInfo.")
        excavatorCacheServer ! excavatorInfo
      }
    case errMessage:Validation => context.parent ! errMessage
    case responseMessage:ResponseMessage => context.parent ! responseMessage

    case excavatorTask:ExcavatorTask =>
      excavatorProccessorServer ! excavatorTask

    case _ =>
  }
}

class ExcavatorProcessorActor(keywordsExtractServer:ActorRef, nerExtractServer:ActorRef)
  extends Actor{

  val log = Logging(context.system, this)
  implicit val formats = Serialization.formats(NoTypeHints)

  val aggregateSearchServer = context.actorOf(Props[AggregateSearchActor], "AggregateSearchActor")
  val templateContentExtractServer = context.actorOf(Props[TemplateContentExtractActor], "TemplateContentExtractActor")

  val zhihusExtractServer = context.actorOf(Props[ZhihuClient], "ZhihuClient")
  val doubanExtractServer = context.actorOf(Props[DoubanClient], "DoubanClient")
  val weiboExtractServer = context.actorOf(Props[SinaWeiboClient], "SinaWeiboClient")
  val baiduBaikeExtractServer = context.actorOf(Props[BaiduBaikeClient], "BaiduBaikeClient")
  val weiboCommentsExtractServer = context.actorOf(Props[SinaWeiboCommentsClient], "SinaWeiboCommentsClient")
  val automaticContentExtractServer = context.actorOf(Props[AutomaticContentExtractActor], "AutomaticContentExtractActor")

  val redisServer = context.actorOf(Props[RedisClients], "RedisClients")
  log.error("Init Excavator Processor server...")

  def receive = {
    case ExcavatorTask(taskName, excavatorInfo) =>
      log.error(s"Start excavator with taskNmae: $taskName, excavatorInfo: $excavatorInfo")
      /*
        Hold the origin sender from outer scope.
        val originalSender = sender()
       */

      context.actorOf(Props(new Actor() {

        var info = excavatorInfo
        val stageStatusName = s"${info.uid}:$taskName"

        var contentResult:Option[NewsContent] = None
        var searchResult:Option[SearchItems] = None

        var nersResult:Option[Ners]= None
        var keywordsResult:Option[Keywords] = None
        var weibosResult:Option[Weibos] = None

        var zhihusResult:Option[Zhihus]= None
        var baikeResult:Option[BaiduBaike] = None
        var doubanResult:Option[Douban] = None
        var weiboCommentsResult:Option[WeiboComments] = None

        def receive = {
          /*
            Media result from automatic extractor,
            maybe has content, no content but title, neither content nor title.
           */
          case MediaResult("AutomaticExtractor", newsContent) => newsContent match {
            case NewsContent(url,title,_,_,_,_,_,_,content) if url.nonEmpty && title.nonEmpty && content.nonEmpty=>
              contentResult = Some(newsContent)
              updateExcavatorInfoKey(title)
              aggregateSearchServer ! ExtractAggregateSearch(info.key)
            case NewsContent(url,title,_,_,_,_,_,_,content) if url.nonEmpty && title.nonEmpty && content.isEmpty=>
              updateExcavatorInfoKey(title)
              aggregateSearchServer ! ExtractAggregateSearch(info.key)
            case NewsContent(url,title,_,_,_,_,_,_,content) if url.isEmpty && title.isEmpty && content.isEmpty=>
              info match {
                case ExcavatorInfo(_,_,_,key) if key.nonEmpty =>
                  aggregateSearchServer ! ExtractAggregateSearch(info.key)
                /*
                No content get form TemplateExtractor, stop excavator.
                 */
                case _ => // No content get form AutomaticExtractor and no key, stop excavator.
              }
          }

          /*
            Search items form aggregate server.
           */
          case searchItems:SearchItems => contentResult match {
            case Some(_) =>
              searchResult = Some(searchItems)                  // search && content done.
              checkAndUpdateStageTableForContentAndSearch()
              startNLPTasks()                                   // start nlp tasks.
            case _ =>
              searchResult = Some(searchItems)
              templateContentExtractServer ! ExtractContentByTemplate(searchItems)
          }

          /*
            Media result from template extractor.
           */
          case MediaResult("TemplateExtractor", newsContent) => newsContent match {
            case NewsContent(url,title,_,_,_,_,_,_,content) if url.nonEmpty && title.nonEmpty && content.nonEmpty=>
              contentResult = Some(newsContent)                 // search && content done.
              checkAndUpdateStageTableForContentAndSearch()
              updateExcavatorInfoKey(title)
              startNLPTasks()                                   // start nlp tasks.
            /*
              No content get form TemplateExtractor, stop excavator.
             */
            case _ =>
          }

          /*
            Keywords from keywords extractor,
            and start Zhihu extractor with keywords.
           */
          case keywords:Keywords =>
            keywordsResult = Some(keywords)
            checkAndUpdateStageTableForNerAndKeywordAndWeibo()
            if(keywords.keywords.nonEmpty)
              zhihusExtractServer ! ExtractZhihus(keywords.keywords.mkString(" "))
            else{
              zhihusResult = Some(Zhihus(List[Zhihu]()))
              checkAndUpdateStageTableForAllField()
            }

          /*
            Ners form ner extractor,
            and start baike and douban extractor with ners.
           */
          case ners:Ners =>
            nersResult = Some(ners);  checkAndUpdateStageTableForNerAndKeywordAndWeibo()
            val nerWordList = ners.person ::: ners.org ::: ners.loc ::: ners.gpe ::: ners.time ::: Nil
            if(nerWordList.nonEmpty){
              baiduBaikeExtractServer ! ExtractBaiduBaike(nerWordList.head)
              doubanExtractServer     ! ExtractDouban(nerWordList.head)
            }
            else{
              baikeResult = Some(BaiduBaike("","",""))
              doubanResult = Some(Douban("",""))
              checkAndUpdateStageTableForAllField()
            }

          /*
            Weibo list from weibo extractor,
            and start weibo comments extractor with weibo list.
           */
          case weibos:Weibos =>
            weibosResult = Some(weibos);  checkAndUpdateStageTableForNerAndKeywordAndWeibo()
            if(weibos.weibos.nonEmpty) weiboCommentsExtractServer ! ExtractWeiboComments(weibos)
            else {
              weiboCommentsResult = Some(WeiboComments(List[WeiboComment]()))
              checkAndUpdateStageTableForAllField()
            }

          /*
            Weibo comments list from weibo comments extractor.
           */
          case weiboComments:WeiboComments =>
            weiboCommentsResult = Some(weiboComments)
            checkAndUpdateStageTableForAllField()

          /*
            Zhihu list from zhihu extractor.
           */
          case zhihus:Zhihus =>
            zhihusResult = Some(zhihus)
            checkAndUpdateStageTableForAllField()

          /*
            Baibu baike from baike extractor.
           */
          case baiduBaike:BaiduBaike =>
            baikeResult = Some(baiduBaike)
            checkAndUpdateStageTableForAllField()

          /*
            Douban from douban extractor.
           */
          case douban:Douban =>
            doubanResult = Some(douban)
            checkAndUpdateStageTableForAllField()

          /*
            Process timeout, stop excavator.
           */
          case AccessTimeout =>
            log.error(s"Excavator timeout with excavatorInfo: $excavatorInfo")
            selfShutDown()

          case _ =>
          }

        /*
          If this excavator starts with a url, and the excavator info's key is empty,
          when get a title from automatic or template content extractor,
          update the excavator info's key with this title.
         */
        def updateExcavatorInfoKey(title:String) = {
          if(info.key.isEmpty) info = info.copy(key=title)
        }

        /*
          Check stage fileds with news content and search items, if done, update to redis.
         */
        def checkAndUpdateStageTableForContentAndSearch() = {
          (contentResult,searchResult) match {
            case (Some(_),Some(_)) =>                                   // media & search
              log.error("newsContent,searchItems tasks done!")
              redisServer ! HashTableList(
                HashTable(stageStatusName, "newsContent", write(contentResult.get)) ::
                  HashTable(stageStatusName, "searchItems", write(searchResult.get.searchItems)) ::
                  HashTable(stageStatusName, "status", "2") :: Nil)
            case _ =>
          }
        }

        /*
          Check stage fileds with ners and keywords and weibo, if done, update to redis.
         */
        def checkAndUpdateStageTableForNerAndKeywordAndWeibo() = {
          (nersResult,keywordsResult,weibosResult) match {
            case (Some(_),Some(_),Some(_)) =>
              log.error("ner,keywords,weibo tasks done!")
              redisServer ! HashTableList(
                HashTable(stageStatusName, "ner", write(nersResult.get)) ::
                  HashTable(stageStatusName, "keywords", write(keywordsResult.get.keywords)) ::
                  HashTable(stageStatusName, "weibo", write(weibosResult.get.weibos)) ::
                  HashTable(stageStatusName, "status", "3") :: Nil)
            case _ =>
          }
        }

        /*
          Check stage fileds with zhihu and baike and douban and weobo comments, if done, update to redis.
         */
        def checkAndUpdateStageTableForAllField() = {
          (zhihusResult,baikeResult,doubanResult,weiboCommentsResult) match {
            case (Some(_), Some(_), Some(_), Some(_)) =>
              log.error("zhihu,baike,douban,comments, all tasks done!")
              redisServer ! HashTableList(
                HashTable(stageStatusName, "zhihu", write(zhihusResult.get.zhihus)) ::
                  HashTable(stageStatusName, "baike", write(baikeResult.get)) ::
                  HashTable(stageStatusName, "douban", write(doubanResult.get)) ::
                  HashTable(stageStatusName, "comments", write(weiboCommentsResult.get.comments)) ::
                  HashTable(stageStatusName, "status", "0") :: Nil) // "8" -> "0"
              UpdateStageTableToMongo()
              selfShutDown()
            case _ =>
          }
        }

        /*
          If all sub tasks done, update to mongodb.
         */
        def UpdateStageTableToMongo() = {

          val lineList:ArrayBuffer[Line] = ArrayBuffer()
          contentResult.get.content.foreach {
            case bolck: TextBlock => lineList.append(Line(lineList.size.toString, "txt", bolck.text))
            case bolck: ImageBlock => lineList.append(Line(lineList.size.toString, "img", bolck.src))
            case bolck: ImageInfoBlock => lineList.append(Line(lineList.size.toString, "img_info", bolck.text))
          }

          val doc = NewsItem(
            BSONObjectID.generate.stringify,
            excavatorInfo.uid,
            excavatorInfo.url,
            excavatorInfo.key,
            excavatorInfo.aid,
            contentResult.get.url,
            contentResult.get.title,
            keywordsResult.get.keywords.mkString(","),
            contentResult.get.source,
            contentResult.get.sourceUrl,
            contentResult.get.author,
            contentResult.get.updateTime,
            DateUtils.getCurrentDate,
            contentResult.get.imageNum.toString,
            Lines(lineList.toList),
            searchResult.get,
            nersResult.get,
            weibosResult.get,
            zhihusResult.get,
            baikeResult.get,
            doubanResult.get,
            weiboCommentsResult.get
          )
          import models.NewsItem._
          ReactiveMongo.insertMongo("testForReactiveMongoV2", BSON.write(doc))
        }

        /*
          Shutdown this sub actor.
         */
        def selfShutDown() = {
          log.error(s"ShutDown sub actor: info: $info")
          context.stop(self)
        }

        /*
          Start nlp tasks.
         */
        def startNLPTasks() = {
          weiboExtractServer    ! ExtractWeibos(info.key)
          keywordsExtractServer ! ExtractKeywords(info.key)
          nerExtractServer      ! ExtractNers(info.key)
        }

        /*
          Start excavator.
         */
        taskName match {
          case x:String if x.startsWith("url_") =>
            automaticContentExtractServer  ! ExtractContentByAutomatic(excavatorInfo.url)
          case x:String if x.startsWith("key_") =>
            aggregateSearchServer          ! ExtractAggregateSearch(excavatorInfo.key)
          case _ =>
        }

        import context.dispatcher
        val timeoutMessager = context.system.scheduler.scheduleOnce(180.seconds, self, AccessTimeout)

      }))

    case _ =>
  }

}
