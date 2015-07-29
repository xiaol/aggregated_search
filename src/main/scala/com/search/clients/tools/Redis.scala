package com.search.clients.tools

// Created by ZG on 15/7/26.
//

import com.redis._
import serialization._
import Parse.Implicits._

object Redis{

  def hmSet(setNameAndSearchKey:String, fields: Map[String, String]) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hmset(setNameAndSearchKey, fields)
  }

  def hmGet(setNameAndSearchKey:String, field:String) = {
    val r = new RedisClient("121.41.75.213", 6379)
    r.hmget[String, String](setNameAndSearchKey, field)
  }

}
