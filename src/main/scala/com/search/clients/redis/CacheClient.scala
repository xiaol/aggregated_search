package com.search.clients.redis

import akka.actor.{Props, ActorRef, Actor}

import com.search._
import com.search.clients.redis.RedisClients._
import com.search.clients.tools.DateUtils
import com.search.clients.tools.Base64Utils

// Created by ZG on 15/7/31.
// 

class CacheClient extends Actor{

  private val hashTableExcavator = "ExcavatorItems"

  val redisServer = context.actorOf(Props[RedisClients])

  def receive = {
    case excavatorInfo:ExcavatorInfo => checkAndUpdateCache(sender(), excavatorInfo)

    case _ =>
  }

  private def checkAndUpdateCache(sender: ActorRef, info: ExcavatorInfo) = {
    val (uid, key, alid) = (info.uid, info.key, info.album)
    val url = Base64Utils.DecodeBase64(info.url)

    val urlTask = if (url.nonEmpty) s"url_$url" else ""
    val keyTask = if (key.nonEmpty) s"key_$key" else ""
    val statusTableName = if(urlTask.nonEmpty) s"$uid:$urlTask" else s"$uid:$keyTask"

    val item = hmGet(hashTableExcavator, urlTask, keyTask).get

    // no cache found
    if(item.isEmpty){
      // url task
      if(urlTask.nonEmpty){
        sender      ! ExcavatorWithUrlOrKey(ExcavatorInfo(uid, alid, url, key))
        redisServer ! HashTableList(List(HashTable(hashTableExcavator, urlTask, uid),
          HashTable(statusTableName, "user_id", uid),
          HashTable(statusTableName, "search_key", key),
          HashTable(statusTableName, "search_url", url),
          HashTable(statusTableName, "alid", alid),
          HashTable(statusTableName, "status", "1"),
          HashTable(statusTableName, "createTime", DateUtils.getCurrentDate)
          ))
        sender      ! ResponseMessage(s"$uid:$urlTask")
      }
      else{
        sender      ! ExcavatorWithKey(ExcavatorInfo(uid, alid, url, key))
        redisServer ! HashTableList(HashTable(hashTableExcavator, keyTask, uid) ::
          HashTable(statusTableName, "user_id", uid) ::
          HashTable(statusTableName, "search_key", key) ::
          HashTable(statusTableName, "search_url", url) ::
          HashTable(statusTableName, "alid", alid) ::
          HashTable(statusTableName, "status", "1") ::
          HashTable(statusTableName, "createTime", DateUtils.getCurrentDate) :: Nil)
        sender      ! ResponseMessage(s"$uid:$keyTask")
      }
    }
    // cache found
    else{
      val userIDsOfUrlTask = item.getOrElse(urlTask, "")
      val userIDsOfKeyTask = item.getOrElse(keyTask, "")

      if(userIDsOfUrlTask.nonEmpty){
        val NewUserIDs = s"$userIDsOfUrlTask&$uid"
        redisServer ! HashTable(hashTableExcavator, urlTask, NewUserIDs)
        sender      ! ResponseMessage(s"${userIDsOfUrlTask.split("&").head}:$urlTask")
        println(s"Shot cache and update $urlTask : $NewUserIDs")
      }
      else if(userIDsOfKeyTask.nonEmpty){
        val NewUserIDs = s"$userIDsOfKeyTask&$uid"
        redisServer ! HashTable(hashTableExcavator, keyTask, NewUserIDs)
        sender      ! ResponseMessage(s"${userIDsOfKeyTask.split("&").head}:$keyTask")
        println(s"Shot cache and update $keyTask : $NewUserIDs")
      }
      // load cache error
      else{
        sender ! Validation("LoadCacheError")
      }
    }
  }
}