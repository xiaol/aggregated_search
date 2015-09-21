package cores

// Created by ZG on 15/9/9.
//

/*
  Messages for access timeout.
 */
case object AccessTimeout

/*
  Messages for http response.
  Any case class would use as a http response should extends trait RestMessage.
 */
trait RestMessage
case class Error(message: String) extends RestMessage
case class Validation(messgae: String)
case class ResponseMessage(key: String) extends RestMessage

/*
  Messages for aggregate search: start task and search results.
 */
case class ExtractAggregateSearch(key: String) extends RestMessage
case class SearchItem(url: String, title: String)
case class SearchItems(searchItems: List[SearchItem]) extends RestMessage

/*
  Messages for content extractor: automatic or template actor.
 */
case class ExtractContentByAutomatic(url: String)
case class ExtractContentByTemplate(searchItems: SearchItems) extends RestMessage
case class ExtractContentByTemplateWithUrls(urls:String) extends RestMessage

/*
  Messages for excavator.
  ExcavatorInfo encludes user-id,album-id,url and key.
  ExcavatorTask encludes the task's name like "url_http://xxx.com" or "key_xxxx",
  the "url_" or "key_" means this task create by url or title,
  when the excacator accept this task,
  if "url_", try to extractContentByAutomatic first,
  if "key_", try to extractContentByTemplate first,
 */
case class ExcavatorInfo(uid:String, aid:String, url:String, key:String) extends RestMessage
case class ExcavatorTask(taskNmae:String, excavatorInfo: ExcavatorInfo)