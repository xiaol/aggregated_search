package com.search.clients.redis

// Created by ZG on 15/8/4.
//


import akka.actor.Actor
import com.redis._
import serialization._
import Parse.Implicits._

import RedisClients._

class RedisClients extends Actor{

  def receive = {
    case HashTableList(tasks) =>
      println("get HashTableList")
      hmSet(tasks)
    case HashTable(tableName, key, value) => hSet(tableName, key, value)

  }

}

object RedisClients{

  case class HashTable(tableName:String, key:String, value:String)
  case class HashTableList(tasks:List[HashTable])

  private val HOST = "121.41.75.213"
  private val PORT = 6379

  def hmSet(tasks:List[HashTable]) = {
    val clients = new RedisClientPool(HOST, PORT)
    clients.withClient {
      client => {
        tasks.foreach(task =>
          client.hset(task.tableName, task.key, task.value)
        )
      }
    }
  }

  def hSet(hashTableName:String, fieldName:String, fieldValue:String) = {
    val r = new RedisClient(HOST, PORT)
    r.hset(hashTableName, fieldName, fieldValue)
  }

  def hmGet(hashTableName:String, field:String) = {
    val r = new RedisClient(HOST, PORT)
    r.hmget[String, String](hashTableName, field)
  }

  def hmGet(hashTableName:String, fieldName:String, fieldName1:String) = {
    val r = new RedisClient(HOST, PORT)
    r.hmget[String, String](hashTableName, fieldName, fieldName1)
  }

  def hGetAll(hashTableName:String) = {
    val r = new RedisClient(HOST, PORT)
    r.hgetall[String, String](hashTableName)
  }

}