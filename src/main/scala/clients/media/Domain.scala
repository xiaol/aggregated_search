package clients.media

import cores.RestMessage

// Created by ZG on 15/9/11.
// 

/*
  Domain of media clients.
 */
trait ContentBlock
case class TextBlock(text:String) extends ContentBlock
case class ImageBlock(src:String) extends ContentBlock
case class ImageInfoBlock(text:String) extends ContentBlock
case class NewsContent(url:String, title:String, tags:String, source:String, sourceUrl:String,
                       author:String, updateTime:String, imageNum:Int, content:List[ContentBlock])

/*
  Messages to start the media extractor, and the media result.
 */
case class ExtractMedia(url:String)
case class MediaResult(mediaName:String, newsContent: NewsContent) extends RestMessage