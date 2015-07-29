package com.process

// Created by ZG on 15/7/28.
//

import java.io.File

import Extractors.Extractors
import FileUtil.Log
import Trunk.{DepRules, DepTree, Gotrunk, Keyven}
import spray.json._
import spray.json.DefaultJsonProtocol
import scala.collection.mutable.ArrayBuffer

case class Sentence(sentence:String, result:String)

object SentenceJsonProtocol extends DefaultJsonProtocol{
  implicit val sentenceFormat = jsonFormat2(Sentence)
}

object SentenceCompressor extends Extractors{

  Extractors.setModel("/work/pro/src/main/resources/chinesePCFG.ser", "/work/pro/src/main/resources/userwords.txt")
  val rulefile = new DepRules()
  rulefile.reader("/work/pro/src/main/resources/deprules.txt")

  def trunkhankey(str: String, log: Boolean) = {

    if (log) {
      Log.log("_IKeyven.Function.trunkhankey")
    }
    //    val xyz: String = str.replaceAll("前书名号","《")
    //    val uvw: String = xyz.replaceAll("后书名号","》")
    val trf: String = str.replaceAll("\"", "_")
    val hjs: String = str.replaceAll(" " ,",")
    val sssssss: String = hjs.replaceAll("百分号","%")
    val ssssss: String = sssssss.replaceAll("，",",")
    val sssss: String = ssssss.replaceAll("：",":")
    // remove parentheses and texts within them
    val ssss: String = sssss.replaceAll("\\(.*\\)", "")
    val wud: String = ssss.replaceAll("\\（.*\\）", "")
    val sss: String = wud.replaceAll("；",";")
    val ss: String = sss.replaceAll("\\【.*\\】", "")
    val s:  String = ss.replaceAll("\\（.*\\）", "")
    val str_segs: Array[String] = s.split(",|:|;|、")
    var st_array = ArrayBuffer[String]()
    for (st <- str_segs){
      val tree: DepTree = new DepTree(st)
      val result: String = Gotrunk.gotrunk(tree, rulefile, log)
      val st_compr: String = Keyven.keyven(result, tree)
      st_array += st_compr
    }
    val result: String = st_array.mkString(",")
    // sentence compression failure check
    import SentenceJsonProtocol._
    if (result.length() >= s.length()){
      Sentence(str, s).toJson.toString()
    }else{
      Sentence(str, result).toJson.toString()
    }
  }
}
