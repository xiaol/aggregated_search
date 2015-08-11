package com.search.clients.tools

import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, Document}
import org.jsoup.select.Elements

// Created by ZG on 15/7/23.
// 

object Extractor{
  def getMetaContentByName(doc:Document, name:String) = {
    doc.select(s"meta[name=$name]").first().attr("content")
  }

  def getMentBySels(doc:Document, sels:List[String]) = {
    var ment:Element = null
    def extract() {
      for (sel <- sels) {
        ment = doc.select(sel).first()
        if (ment==null)
          return
      }
    }
    extract()
    ment
  }

  def getMentsBySels(doc:Document, sels:List[String]) = {
    var ment:Element = null
    var ments:Elements = null
    def extract() {
      for (sel <- sels) {
        ments = doc.select(sel)
        println(ments)
        for(m <- ments.toArray){
          println(m)
          val node = Jsoup.parse(m.toString)
          if(node.hasText){
            println(node.text())
          }
        }
        if (ment==null)
          return
      }
    }
    extract()
    ment
  }
}
