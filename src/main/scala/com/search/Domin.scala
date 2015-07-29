package com.search

// Created by ZG on 15/7/20.
// 

trait Domin {

}

// message
trait RestMessage
case class SearchWithKey(uid:String, key:String) extends RestMessage
case class ExtractWithUrl(key:String) extends RestMessage
case class TestResults(key:String) extends RestMessage

// url -> title mappping
case class UrlMapTitleItem(url:String, title:String)
case class UrlMapTitleItems(items:List[UrlMapTitleItem])
case class SearchItemsWithInfo(userId:String, searchKey:String, items:List[UrlMapTitleItem]) extends RestMessage

// error
case class Error(message:String) extends RestMessage
case class Validation(messgae:String)


// search result
trait SearchClientItems


// news content
trait ContentBlock
case class TextBlock(text:String) extends ContentBlock
case class ImageBlock(src:String) extends ContentBlock
case class ImageInfoBlock(text:String) extends ContentBlock
case class NewsContent(url:String, title:String, tags:String, source:String, sourceUrl:String,
                       author:String, updateTime:String, imageNum:Int, content:List[ContentBlock])
case class ExtractResult(mediaName:String, newsContent: NewsContent)
