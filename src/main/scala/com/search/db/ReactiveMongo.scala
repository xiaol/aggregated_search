package com.search.db

// Created by ZG on 15/7/26.
//

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{MongoConnectionOptions, MongoDriver}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import reactivemongo.api.commands.WriteResult

import scala.util.{ Failure, Success }

object ReactiveMongo {

  def insertMongo(document:BSONDocument) = {
    val driver = new MongoDriver
    //  val conOpts = MongoConnectionOptions(mongo.options.)
    val servers = List("121.41.49.44:27017", "121.41.75.213:27017", "121.41.112.241:27017")
    val connection = driver.connection(servers)
    val dbs = connection("news_ver2")
    val collection = dbs.collection[BSONCollection]("testForReactiveMongo")

    val future: Future[WriteResult] = collection.insert(document)

    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }
  }
}
