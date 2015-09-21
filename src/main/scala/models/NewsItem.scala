package models

// Created by ZG on 15/7/26.
//

import nlps.Ners
import clients.community._
import reactivemongo.bson._
import cores.{SearchItem,SearchItems}

case class Line(bolckNumber:String, blockName:String, blockEntity:String)
case class Lines(lines:List[Line])

case class NewsItem(id: String,
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
                    searchItems:SearchItems,
                    ners:Ners,
                    weibos:Weibos,
                    zhihus:Zhihus,
                    baike:BaiduBaike,
                    douban:Douban,
                    comment:WeiboComments
                     )

object NewsItem {

  implicit object LineWriter extends BSONDocumentWriter[Line] {
    def write(line: Line): BSONDocument = BSONDocument(
      line.bolckNumber -> BSONDocument(line.blockName->line.blockEntity))
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

  implicit object SearchItemWriter extends BSONDocumentWriter[SearchItem] {
    def write(searchItem: SearchItem): BSONDocument = BSONDocument(
      searchItem.url -> searchItem.title
    )
  }

  implicit object SearchItemsWriter extends BSONDocumentWriter[SearchItems] {
    def write(searchItems: SearchItems): BSONDocument = BSONDocument(
      "searchItems"->BSONArray(searchItems.searchItems)
    )
  }

  implicit object NersWriter extends BSONDocumentWriter[Ners] {
    def write(ners: Ners): BSONDocument = BSONDocument(
      "gpe"     ->ners.gpe,
      "loc"     ->ners.loc,
      "org"     ->ners.org,
      "time"    ->ners.time,
      "person"  ->ners.person
    )
  }

  implicit object WeiboWriter extends BSONDocumentWriter[Weibo] {
    def write(weibo: Weibo): BSONDocument = BSONDocument(
      "url"             -> weibo.url,
      "title"           -> weibo.content,
      "updateTime"      -> weibo.updateTime,
      "user"            -> weibo.sourceName,
      "like_count"      -> weibo.likesCount,
      "reposts_count"   -> weibo.repostsCount,
      "comments_count"  -> weibo.commentsCount,
      "profileImageUrl" -> weibo.profileImgUrl,
      "img"             -> weibo.imgUrl,
      "imgs"            -> weibo.imgUrls,
      "sourceSitename"  -> "weibo"
    )
  }

  implicit object WeibosWriter extends BSONDocumentWriter[Weibos] {
    def write(weibos: Weibos): BSONDocument = BSONDocument(
      "weibo"->BSONArray(weibos.weibos)
    )
  }

  implicit object ZhihuWriter extends BSONDocumentWriter[Zhihu] {
    def write(zhihu: Zhihu): BSONDocument = BSONDocument(
      "url"   -> zhihu.url,
      "title" -> zhihu.title,
      "user"  -> zhihu.user
    )
  }

  implicit object ZhihusWriter extends BSONDocumentWriter[Zhihus] {
    def write(zhihus: Zhihus): BSONDocument = BSONDocument(
      "zhihu"->BSONArray(zhihus.zhihus)
    )
  }

  implicit object BaiduBaikeWriter extends BSONDocumentWriter[BaiduBaike] {
    def write(baike: BaiduBaike): BSONDocument = BSONDocument(
      "title"     -> baike.title,
      "url"       -> baike.url,
      "abstract"  -> baike.abs
    )
  }

  implicit object DoubanWriter extends BSONDocumentWriter[Douban] {
    def write(douban: Douban): BSONDocument = BSONDocument(
      "title" -> douban.title,
      "url"   -> douban.url
    )
  }

  implicit object WeiboCommentWriter extends BSONDocumentWriter[WeiboComment] {
    def write(comment: WeiboComment): BSONDocument = BSONDocument(
      "created_at"      -> comment.createTime,
      "up"              -> comment.upCount,
      "author_name"     -> comment.authorName,
      "type"            -> "weibo",
      "author_id"       -> comment.authorId,
      "author_img_url"  -> comment.authorImgUrl,
      "weibo_id"        -> comment.weiboId,
      "down"            -> "0",
      "post_id"         -> "uniqueId",
      "message"         -> comment.message,
      "comment_id"      -> comment.commentId
    )
  }

  implicit object WeiboCommentsWriter extends BSONDocumentWriter[WeiboComments] {
    def write(comments: WeiboComments): BSONDocument = BSONDocument(
      "comment" -> BSONArray(comments.comments)
    )
  }

  implicit object ItemWriter extends BSONDocumentWriter[NewsItem] {
    def write(newsItem: NewsItem): BSONDocument = BSONDocument(
      "_id"         -> newsItem.id,
      "user_id"     -> newsItem.userId,
      "search_url"  -> newsItem.searchUrl,
      "search_key"  -> newsItem.searchKey,
      "alid"        -> newsItem.alid,
      "url"         -> newsItem.url,
      "title"       -> newsItem.title,
      "tags"        -> newsItem.tags,
      "source"      -> newsItem.source,
      "source_url"  -> newsItem.sourceUrl,
      "author"      -> newsItem.author,
      "update_time" -> newsItem.updateTime,
      "create_time" -> newsItem.createTime,
      "imgnum"      -> newsItem.imageNum,
      "content"     -> newsItem.content.lines,
      "aggre_items" -> newsItem.searchItems.searchItems,
      "ner"         -> newsItem.ners,
      "weibo"       -> newsItem.weibos.weibos,
      "zhihu"       -> newsItem.zhihus.zhihus,
      "baike"       -> newsItem.baike,
      "douban"      -> newsItem.douban,
      "comment"     -> newsItem.comment.comments
    )
  }
}
