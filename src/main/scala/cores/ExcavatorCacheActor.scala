package cores

// Created by ZG on 15/9/14.
//

import akka.actor.{Props, ActorRef, Actor}
import akka.event.Logging
import redis.RedisClients

import utils.{DateUtils, Base64Utils}
import redis.RedisClients._

class ExcavatorCacheActor extends Actor{

  val log = Logging(context.system, this)
  private val hashTableExcavatorItems = "ExcavatorItems"

  def receive = {
    case excavatorInfo:ExcavatorInfo => checkAndUpdateCacheOfExcavatorItems(sender(), excavatorInfo)
    case _ =>
  }

  private def checkAndUpdateCacheOfExcavatorItems(sender: ActorRef, info: ExcavatorInfo) = {

    val redisServer = context.actorOf(Props[RedisClients])

    val (uid, key, aid) = (info.uid, info.key, info.aid)
    val url = Base64Utils.DecodeBase64(info.url)

    val taskName = if (url.nonEmpty) s"url_$url" else s"key_$key"
    val statusTableName = s"$uid:$taskName"

    val item = hmGet(hashTableExcavatorItems, taskName).get
    if(item.isEmpty) {
      log.error(s"No cache found with task: $taskName")

      sender      ! ExcavatorTask(taskName, info.copy(url=url))
      redisServer ! HashTableList(List(HashTable(hashTableExcavatorItems, taskName, uid),
        HashTable(statusTableName, "user_id", uid),
        HashTable(statusTableName, "search_key", key),
        HashTable(statusTableName, "search_url", url),
        HashTable(statusTableName, "alid", aid),
        HashTable(statusTableName, "status", "1"),
        HashTable(statusTableName, "createTime", DateUtils.getCurrentDate)))
      sender      ! ResponseMessage(statusTableName)
    }
    else{
      log.error(s"Cache founded with task: $taskName")

      val uesrIdList = item.getOrElse(taskName, "")

      if(uesrIdList.nonEmpty){
        val newUesrIdList = s"$uesrIdList&$uid"
        log.error(s"Shot cache and update task:$taskName with newUesrIdList:$newUesrIdList")

        redisServer ! HashTable(hashTableExcavatorItems, taskName, newUesrIdList)
        sender      ! ResponseMessage(s"${uesrIdList.split("&").head}:$taskName")
      }
      else{
        log.error(s"Load cache error with task: $taskName")
        sender ! Validation("LoadCacheError")
      }
    }

  }

}
