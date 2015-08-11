package com.search.db

// Created by ZG on 15/7/26.
//

import reactivemongo.bson._

case class Line(
               lineNumber:String,
               tagName:String,
               content:String
                 )

case class Lines(
                  lines:List[Line]
                  )

case class UlrMapTitle(
                      url:String,
                      title:String
                        )

case class AggreItems(
                       aggreItems:List[UlrMapTitle]
                       )

case class Item(id: String,
                userId:String,
                searchUrl:String,
                searchKey:String,
                alid:String,
                url:String,
                title: String,
                tags:String,
                source:String,
                sourceUrl:String,
                author:String,
                updateTime:String,
                createTime:String,
                imageNum:String,
                content:Lines,
                aggreItems:AggreItems
                 )

object NewsItem {

  implicit object LineWriter extends BSONDocumentWriter[Line] {
    def write(line: Line): BSONDocument = BSONDocument(
      line.lineNumber -> BSONDocument(line.tagName->line.content))
  }

  implicit object LineReader extends BSONDocumentReader[Line] {
    def read(doc: BSONDocument): Line = Line(
      doc.getAs[String]("lineNumber").get,
      doc.getAs[String]("tagName").get,
      doc.getAs[String]("content").get
    )
  }

  implicit object LinesWriter extends BSONDocumentWriter[Lines] {
    def write(lines: Lines): BSONDocument = BSONDocument(
      "content"->BSONArray(lines.lines)
    )
  }

  implicit object UlrMapTitleWriter extends BSONDocumentWriter[UlrMapTitle] {
    def write(ulrMapTitle: UlrMapTitle): BSONDocument = BSONDocument(
      ulrMapTitle.url -> ulrMapTitle.title
    )
  }

  implicit object AggreItemsWriter extends BSONDocumentWriter[AggreItems] {
    def write(aggreItems: AggreItems): BSONDocument = BSONDocument(
      "aggreItems"->BSONArray(aggreItems.aggreItems)
    )
  }

  implicit object ItemWriter extends BSONDocumentWriter[Item] {
    def write(item: Item): BSONDocument = BSONDocument(
      "_id" -> item.id,
      "user_id" -> item.userId,
      "search_url" -> item.searchUrl,
      "search_key" -> item.searchKey,
      "alid" -> item.alid,
      "url" -> item.url,
      "title" -> item.title,
      "tags" -> item.tags,
      "source" -> item.source,
      "source_url" -> item.sourceUrl,
      "author" -> item.author,
      "update_time" -> item.updateTime,
      "create_time" -> item.createTime,
      "imgnum" -> item.imageNum,
      "content" -> item.content.lines,
      "aggre_items" -> item.aggreItems.aggreItems
    )
  }
}

class NewsItem {}
