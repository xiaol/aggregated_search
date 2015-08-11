package com.search.clients.tools

// Created by ZG on 15/7/26.
//

import com.redis._
import serialization._
import Parse.Implicits._


case class HashTable(tableName:String, key:String, value:String)

object Redis{

  def hmSet(tasks:List[HashTable]) = {
    val clients = new RedisClientPool("localhost", 6379)
    clients.withClient {
      client => {
        tasks.foreach(task =>
          client.hset(task.tableName, task.key, task.value)
        )
      }
    }
  }

  def hmSet(hashTableName:String, fields: Map[String, String]) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hmset(hashTableName, fields)
  }

  def hSet(hashTableName:String, fieldName:String, fieldValue:String) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hset(hashTableName, fieldName, fieldValue)
  }

  def hmGet(hashTableName:String, field:String) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hmget[String, String](hashTableName, field)
  }

  def hmGet(hashTableName:String, searchUrl:String, searchKey:String) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hmget[String, String](hashTableName, searchUrl, searchKey)
  }
}
