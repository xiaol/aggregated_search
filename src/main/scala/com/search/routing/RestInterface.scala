package com.search.routing

import akka.actor.{Props, Actor}
import com.process.SentenceCompressor
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
  val ipinfoService = context.actorOf(Props[IpinfoClient])

  // search service
  val baiduService = context.actorOf(Props[BaiduClient])
  val bingService = context.actorOf(Props[BingClient])
  val qihooService = context.actorOf(Props[QihooClient])
  val smService = context.actorOf(Props[SmClient])
  val sougouService = context.actorOf(Props[SougouClient])

  // media service
  val neteasyService = context.actorOf(Props[NeteasyClient])
  val sinaService = context.actorOf(Props[SinaClient])
  val sohuService = context.actorOf(Props[SohuClient])
  val tencentService = context.actorOf(Props[TencentClient])
  val shenmaService = context.actorOf(Props[ShenmaClient])

  // content extract service
  val extractService = context.actorOf(Props[ExtractorEngineActor])

  // redis cache service
  val cacheService = context.actorOf(Props[CacheClient])

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
            complete(SentenceCompressor.trunkhankey(sentence, log = false))
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
      }
  }

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
