package mongos

// Created by ZG on 15/9/10.
// 

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{MongoConnectionOptions, MongoDriver}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference

import scala.util.{ Failure, Success }

object ReactiveMongo {

  private val serverList = List("121.41.49.44:27017", "121.41.75.213:27017", "121.41.112.241:27017")
//  private val serverList = "mongodb://121.41.49.44:27017,121.41.75.213:27017,121.41.112.241:27017"

  private val defaultDB = "news_ver2"
//  private val conOpts = MongoConnectionOptions(readPreference = ReadPreference.primary)

  private def getCollection(collectionName:String) = {
    val driver = new MongoDriver()
//    val connection = driver.connection(serverList, options = conOpts)
    val connection = driver.connection(serverList)
    val dbs = connection(defaultDB)
    dbs.collection[BSONCollection](collectionName)
  }

  def insertMongo(collectionName:String, document:BSONDocument) = {

    val collection = getCollection(collectionName)
    val future: Future[WriteResult] = collection.insert(document)
    future.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully inserted document with result: $writeResult")
    }
  }

  def upsertMongo(collectionName:String, selector:BSONDocument, document:BSONDocument) = {

    val collection = getCollection(collectionName)
    val futureUpdate: Future[WriteResult] = collection.update(selector, document, upsert = true)
    futureUpdate.onComplete {
      case Failure(e) => throw e
      case Success(writeResult) =>
        println(s"successfully upserted document with result: $writeResult")
    }
  }
}
