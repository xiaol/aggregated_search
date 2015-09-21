package utils

// Created by ZG on 15/9/10.
// 

import org.jsoup.nodes.{Element, Document}

object Extractor{
  def getMetaContentByName(doc:Document, name:String) = {
    doc.select(s"meta[name=$name]").first().attr("content")
  }

  def getMentBySels(doc:Document, sels:List[String]) = {
    var ment:Element = null
    def extract() {
      for (sel <- sels) {
        ment = doc.select(sel).first()
        if (ment!=null)
          return
      }
    }
    extract()
    ment
  }
}