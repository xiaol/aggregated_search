package cores

// Created by ZG on 15/9/11.
//

import akka.actor.{Props, Actor}
import akka.event.Logging
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import clients.searchengine._

class AggregateSearchActor extends Actor{

  val log = Logging(context.system, this)

  val smClient = context.actorOf(Props[SmClient], "SmClient")
  val bingClient = context.actorOf(Props[BingClient], "BingClient")
  val baiduClient = context.actorOf(Props[BaiduClient], "BaiduClient")
  val qihooClient = context.actorOf(Props[QihooClient], "QihooClient")
  val sougouClient = context.actorOf(Props[SougouClient], "SougouClient")
  log.error("Init aggregate search server...")

  def receive = {
    case ExtractAggregateSearch(key) =>
      log.error(s"Start aggregateSearch with key: $key")
      val originalSender = sender()

      context.actorOf(Props(new Actor() {

        var bingItemsResult:Option[BingItems] = None
        var baiduItemsResult:Option[BaiduItems] = None
        var qihooItemsResult:Option[QihooItems] = None
        var shenmaItemsResult:Option[SmItems] = None
        var sougouItemsResult:Option[SougouItems] = None

        def receive = {
          case bingItems:BingItems => bingItemsResult = Some(bingItems); checkReadyReply()
          case baiduItems:BaiduItems => baiduItemsResult = Some(baiduItems); checkReadyReply()
          case qihooItems:QihooItems => qihooItemsResult = Some(qihooItems); checkReadyReply()
          case shenmaItems:SmItems => shenmaItemsResult = Some(shenmaItems); checkReadyReply()
          case sougouItems:SougouItems => sougouItemsResult = Some(sougouItems); checkReadyReply()
          case AccessTimeout =>
            log.error(s"Access timeout, return without all ready. (key:$key)")
            responseAndShutDown()   // Timeout, return without all ready.
          case _ =>
        }

        def checkReadyReply() = {
          (bingItemsResult,baiduItemsResult,qihooItemsResult,shenmaItemsResult,sougouItemsResult) match {
            case (Some(_),Some(_),Some(_),Some(_),Some(_)) =>
              log.error(s"All clients are completed, return. (key:$key)")
              responseAndShutDown()
            case _ => // Not all ready.
          }
        }

        def responseAndShutDown() = {

          val urlSets = mutable.Set[String]()
          val searchItems:ArrayBuffer[SearchItem] = ArrayBuffer()

          bingItemsResult match {
            case Some(bingItems) => bingItems.items.foreach{ item =>
              if(!urlSets.contains(item.url) && item.url.startsWith("http")){
                searchItems += SearchItem(item.url, item.title)
                urlSets.add(item.url)
              }
            }
            case _ =>
          }
          baiduItemsResult match {
            case Some(baiduItems) => baiduItems.items.foreach{ item =>
              if(!urlSets.contains(item.url) && item.url.startsWith("http")){
                searchItems += SearchItem(item.url, item.title)
                urlSets.add(item.url)
              }
            }
            case _ =>
          }
          qihooItemsResult match {
            case Some(qihooItems) => qihooItems.items.foreach{ item =>
              if(!urlSets.contains(item.url) && item.url.startsWith("http")){
                searchItems += SearchItem(item.url, item.title)
                urlSets.add(item.url)
              }
            }
            case _ =>
          }
          shenmaItemsResult match {
            case Some(shenmaItems) => shenmaItems.items.foreach{ item =>
              if(!urlSets.contains(item.url) && item.url.startsWith("http")){
                searchItems += SearchItem(item.url, item.title)
                urlSets.add(item.url)
              }
            }
            case _ =>
          }
          sougouItemsResult match {
            case Some(sougouItems) => sougouItems.items.foreach{ item =>
              if(!urlSets.contains(item.url) && item.url.startsWith("http")){
                searchItems += SearchItem(item.url, item.title)
                urlSets.add(item.url)
              }
            }
            case _ =>
          }

          originalSender ! SearchItems(searchItems.toList)
          context.stop(self)
        }

        smClient      ! StartSearchEngineWithKey(key)
        bingClient    ! StartSearchEngineWithKey(key)
        baiduClient   ! StartSearchEngineWithKey(key)
        qihooClient   ! StartSearchEngineWithKey(key)
        sougouClient  ! StartSearchEngineWithKey(key)

        import context.dispatcher
        val timeoutMessager = context.system.scheduler.scheduleOnce(30.seconds, self, AccessTimeout)

      }))
    case _ =>
  }

}