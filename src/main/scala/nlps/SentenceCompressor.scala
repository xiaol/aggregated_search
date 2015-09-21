package nlps

// Created by ZG on 15/7/28.
//

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

  Extractors.setModel("src/main/resources/chinesePCFG.ser", "src/main/resources/userwords.txt")
  val rulefile = new DepRules()
  rulefile.reader("src/main/resources/deprules.txt")
//  Extractors.setModel("/work/pro/src/main/resources/chinesePCFG.ser", "/work/pro/src/main/resources/userwords.txt")
//  val rulefile = new DepRules()
//  rulefile.reader("/work/pro/src/main/resources/deprules.txt")

  def trunkhankey(str: String, log: Boolean=false) = {

    if (log) {
      Log.log("_IKeyven.Function.trunkhankey")
    }
    val s: String = str.replaceAll("，|：|；|,|:|;|、", " ").replaceAll("[\\pP\\pS]", "")
    val str_segs = s.trim.split(" ")
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
