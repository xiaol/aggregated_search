package com.search.routing

import akka.actor.{Props, Actor}
import com.process.SentenceCompressor
import spray.routing.{Route, HttpService}

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
  val sohuService = context.actorOf(Props[SinaClient])
  val tencentService = context.actorOf(Props[SinaClient])

  // excavator service
  val excavatorService = context.actorOf(Props[ExcavatorActor])

  val route = {
    path("test") {
      get {
        complete{
          "test ok"
        }
      }
    } ~
    path("search") {
      get {
        parameters('key.as[String], 'uid.as[String]) { (searchKey, userId) =>
          searchWithKey {
            SearchWithKey(userId, searchKey)
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
      }
  }

  def excavatorWithKey(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new ExcavatorActor), message)

  def searchWithKey(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(
      new GetResultsWithkeysActor(baiduService, bingService, qihooService, smService, sougouService,
        excavatorService)), message)

  def extractWithUrl(message: RestMessage):Route =
    ctx => perRequest(ctx, Props(
      new ExtractContentWithUrlActor(sinaService, neteasyService, sohuService, tencentService)), message)
}
