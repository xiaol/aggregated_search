import clients.community._
import clients.media._
import mongos.ReactiveMongo
import cores.{SearchItem, SearchItems}
import nlps.Ners

import org.json4s.native.Serialization
import org.json4s.native.Serialization
import org.json4s._
import org.json4s.native.JsonMethods._
import reactivemongo.bson._
import reactivemongo.bson.{BSON, BSONObjectID}
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter}
import scala.concurrent.ExecutionContext.Implicits.global

// Created by ZG on 15/9/16.

import models.{Line, Lines, NewsItem}
import models.NewsItem._

object TestRedisField extends App{

  implicit val formats = Serialization.formats(NoTypeHints)

  val searchItems = SearchItems(List(SearchItem("u1","t1"),SearchItem("u2","t2"),SearchItem("u3","t3")))
  println(Serialization.write(searchItems))

  println(Serialization.write(searchItems.searchItems))

  val content = NewsContent("url","title","tags","source","sourceUlr","author","updateTime",1,List(TextBlock("text"),ImageBlock("image"),ImageInfoBlock("imageInfo")))

  println(Serialization.write(content))



  val document = NewsItem(
    BSONObjectID.generate.stringify,
    "userID",
    "searchUrl",
    "searchKey",
    "alid",
    "url",
    "title",
    "tags",
    "source",
    "sourceUrl",
    "author",
    "updateTime",
    "createTime",
    "imageNum",
    Lines(List(Line("1","txt","txtcont"),Line("2","img","imgcont"),Line("3","imginfo","imginfocont"))),
    SearchItems(List(SearchItem("url1","tit1"),SearchItem("url2","tit2"),SearchItem("url3","tit3"))),
    Ners(time=List[String]("t1","t2"),gpe=List[String]("g1","g2"),loc=List[String]("l1","l2"),
      person=List[String]("p1","p2"),org=List[String]("o1","o2")),
    Weibos(List(Weibo("url:String", "content:String", "content:String", "content:String",
      "likesCount:String", "repostsCount:String", "commentsCount:String", "commentsCount:String",
      "imgUrl:String", List[String]("img1","img2")))),
    Zhihus(List(Zhihu("u1","t1","us1"),Zhihu("u2","t2","us2"),Zhihu("u3","t3","us3"))),
    BaiduBaike("title","url","abstract"),
    Douban("title","url"),
    WeiboComments(List(WeiboComment("createTime:String", "upCount:String", "authorName:String", "authorId:String",
      "authorImgUrl:String", "weiboId:String", "commentId:String", "message:String")))
  )

  import models.NewsItem._
  ReactiveMongo.insertMongo("testForReactiveMongoV2", BSON.write(document))


}
