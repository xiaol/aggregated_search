package routers

//import com.search._
//import com.search.clients.media._
//import com.search.clients.redis.CacheClient
//import com.search.clients.rest._
//import com.search.clients.search._
//import com.search.core._
//import com.process.{KeywordsExtractor, SentenceCompressor, NerExtractor}
//import com.process.{KeywordsExtractor, NerExtractor}

import utils.Base64Utils

import scala.language.postfixOps
import akka.actor.{Actor, Props}
import spray.routing.{HttpService, Route}

import nlps._
import cores._

class RestInterface extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  // aggregate search service
  val aggregateSearchServer = context.actorOf(Props[AggregateSearchActor], "AggregateSearchActor")

  // template content extractor service
  val templateContentExtractServer = context.actorOf(Props[TemplateContentExtractActor], "TemplateContentExtractActor")

  // nlp service
  val nerClient = context.actorOf(Props[NerExtractorActor], "NerExtractorActor")
  val keywordsClient = context.actorOf(Props[KeywordsExtractorActor], "KeywordsExtractorActor")

  // excavator cache service
  val excavatorCacheClient = context.actorOf(Props[ExcavatorCacheActor], "ExcavatorCacheActor")

  // excavator processor service
  val excavatorProcessorClient = context.actorOf(Props(new ExcavatorProcessorActor(
    keywordsClient,nerClient)), "ExcavatorProcessorActor")


//  // content extract service
//  val extractService = context.actorOf(Props[ExtractorEngineActor], "extractService")
//
//  // redis cache service
//  val cacheService = context.actorOf(Props[CacheClient], "cacheService")

//  val testNestedSubActor = context.actorOf(Props[TestNestedSubActor], "testNestedSubActor")

  val route = {
    path("test") {
      get {
        complete{
            "Test OK."
        }
      }
    }~
      path("excavator") {
        get {
          parameters('uid.as[String], 'album.as[String], 'url.as[String]?, 'key.as[String]?) {
            (userId, album, searchUrl, searchKey) =>
              startExcavator{
                ExcavatorInfo(userId, album, searchUrl.getOrElse(""), searchKey.getOrElse(""))
              }
          }
        }
      } ~
      path("search") {
        get {
          parameters('key.as[String]) { searchKey =>
            startAggregateSearch {
              ExtractAggregateSearch(searchKey)
            }
          }
        }
      } ~
      path("extracter") {
        get {
          parameters('urls.as[String]) { urlList =>
            startExtractTemplateContent {
              ExtractContentByTemplateWithUrls(urlList)
            }
          }
        }
      } ~
      path("SentenceCompressor") {
        get {
          parameters('sentence) { sentence =>
            complete(SentenceCompressor.trunkhankey(sentence, log = false))
          }
        }
      } ~
      path("keywords") {
        get {
          parameters('s) { sentence =>
            startExtractKeywords{
              ExtractKeywords(sentence)
            }
          }
        }
      } ~
      path("ner") {
        get {
          parameters('s) { sentence =>
            startExtractNer{
              ExtractNers(sentence)
            }
          }
        }
      }
  }

  def startExcavator(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new ExcavatorServiceActor(excavatorCacheClient, excavatorProcessorClient)), message)

  def startAggregateSearch(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new AggregateSearchActor), message)

  def startExtractTemplateContent(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new TemplateContentExtractServer(templateContentExtractServer)), message)

  def startExtractKeywords(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new KeywordsExtractorActorServer(keywordsClient)), message)

  def startExtractNer(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new NerExtractorActorServer(nerClient)), message)
}
