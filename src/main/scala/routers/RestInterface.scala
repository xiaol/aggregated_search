package routers

import akka.actor.{Props, Actor}
import cores.{TestNestedSubActor, TestNestedActor}

//import com.process.{KeywordsExtractor, SentenceCompressor, NerExtractor}
import com.process.{KeywordsExtractor, NerExtractor}
import nlps.SentenceCompressor
import com.search.clients.redis.CacheClient
import spray.routing.{Route, HttpService}
import scala.language.postfixOps

import com.search.clients.search._
import com.search.clients.rest._
import com.search.clients.media._
import com.search.core._
import com.search._

class RestInterface extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  // rest service
  val ipinfoService = context.actorOf(Props[IpinfoClient], "ipinfoService")

  // search service
  val baiduService = context.actorOf(Props[BaiduClient], "baiduService")
  val bingService = context.actorOf(Props[BingClient], "bingService")
  val qihooService = context.actorOf(Props[QihooClient], "qihooService")
  val smService = context.actorOf(Props[SmClient], "smService")
  val sougouService = context.actorOf(Props[SougouClient], "sougouService")

  // media service
  val neteasyService = context.actorOf(Props[NeteasyClient], "neteasyService")
  val sinaService = context.actorOf(Props[SinaClient], "sinaService")
  val sohuService = context.actorOf(Props[SohuClient], "sohuService")
  val tencentService = context.actorOf(Props[TencentClient], "tencentService")
  val shenmaService = context.actorOf(Props[ShenmaClient], "shenmaService")

  // content extract service
  val extractService = context.actorOf(Props[ExtractorEngineActor], "extractService")

  // redis cache service
  val cacheService = context.actorOf(Props[CacheClient], "cacheService")

  val testNestedSubActor = context.actorOf(Props[TestNestedSubActor], "testNestedSubActor")

  val route = {
    path("test") {
      get {
        complete{
            "Test OK."
        }
      }
    } ~
    path("search") {
      get {
        parameters('key.as[String]) { searchKey =>
          searchWithKey {
            SearchWithKey(searchKey)
          }
        }
      }
    } ~
      path("extracter") {
        get {
          parameters('urls.as[String]) { searchKey =>
            extractContentWithSearchItems {
              SearchWithKey(searchKey)
            }
          }
        }
//      path("extracter" / Segment) { seg =>
//        extractContentWithSearchItems{
//          SearchWithKey(seg)
//        }
      } ~
      path("SentenceCompressor") {
        get {
          parameters('sentence) { sentence =>
//            complete(SentenceCompressor.trunkhankey(sentence, log = false))
            complete(SentenceCompressor.trunkhankey(sentence, log = false))
          }
        }
      } ~
      path("ner") {
        get {
          parameters('s) { sentence =>
            complete(NerExtractor.getNerResponse(sentence))
          }
        }
      } ~
      path("keywords") {
        get {
          parameters('s) { sentence =>
            complete(KeywordsExtractor.getKeywordsResponse(sentence))
          }
        }
      } ~
      path("excavator") {
        get {
          parameters('uid.as[String], 'album.as[String], 'url.as[String]?, 'key.as[String]?) {
            (userId, album, searchUrl, searchKey) =>
              excavatorWith{
                ExcavatorInfo(userId, album, searchUrl.getOrElse(""), searchKey.getOrElse(""))
              }
          }
        }
      } ~
      path("testnest") {
        get {
          parameters('s) { sentence =>
            testNestedActorWith{
              SearchWithKey(sentence)
            }
          }
        }
      }
  }

  def testNestedActorWith(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new TestNestedActor(testNestedSubActor)), message)

  def excavatorWith(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new ExcavatorEngineActor(extractService, cacheService)), message)

  def searchWithKey(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new AggregateSearchWithKeyActor(baiduService, bingService, qihooService, smService, sougouService)), message)

  def extractContentWithSearchItems(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new ExtractContentWithSearchItemsActor(neteasyService, sinaService, sohuService, tencentService, shenmaService)), message)
}
