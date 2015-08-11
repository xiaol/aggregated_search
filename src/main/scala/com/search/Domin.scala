package com.search

// Created by ZG on 15/7/20.
//

// message
trait RestMessage
case class SearchWithKey(key:String) extends RestMessage
//case class ExtractWithUrl(key:String) extends RestMessage
case class TestResults(key:String) extends RestMessage

// message for ExcavatorEngineActor
case class ExcavatorInfo(uid:String, album:String, url:String, key:String) extends RestMessage
case class ExcavatorWithUrlOrKey(excavatorInfo:ExcavatorInfo) extends RestMessage
case class ExcavatorWithKey(excavatorInfo:ExcavatorInfo) extends RestMessage

// message for ExtractorEngineActor
case class ExtractWithSearchItems(excavatorInfo:ExcavatorInfo, searchItems:List[UrlMapTitleItem])

// message for AggregateSearchEngineActor
case class UrlMapTitleItem(url:String, title:String)
case class UrlMapTitleItems(items:List[UrlMapTitleItem]) extends RestMessage
case class StartSearchEngineWithKey(key:String)

case class AggregateSearchTask(content:Boolean, excavatorInfo:ExcavatorInfo)
case class AggregateSearchResult(aggregateSearchTask:AggregateSearchTask, items:List[UrlMapTitleItem])

// message for response
case class Error(message:String)
case class Validation(messgae:String)
case class ResponseMessage(key:String) extends RestMessage

// news content
trait ContentBlock
case class TextBlock(text:String) extends ContentBlock
case class ImageBlock(src:String) extends ContentBlock
case class ImageInfoBlock(text:String) extends ContentBlock
case class NewsContent(url:String, title:String, tags:String, source:String, sourceUrl:String,
                       author:String, updateTime:String, imageNum:Int, content:List[ContentBlock])
case class ExtractResult(mediaName:String, newsContent: NewsContent)  extends RestMessage
case class StartExtractMediaWithUrl(url:String)

// message for ExtractorEngineActor
case class AutomaticExtractResult(excavatorInfo:ExcavatorInfo, extractResult: ExtractResult) extends RestMessage
case class TemplateExtractResult(excavatorInfo:ExcavatorInfo, extractResult: ExtractResult) extends RestMessage
