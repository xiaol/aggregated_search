
// Created by ZG on 15/9/14.
//

import akka.actor.{ActorSystem, Props}

import nlps._
import cores._
import clients.media._
import clients.community._
import clients.searchengine._
import org.ansj.splitWord.analysis
import org.ansj.splitWord.analysis.NlpAnalysis

object TestExcavatorActor extends App{

  implicit val system = ActorSystem("TestExcavatorActor")




  Thread.sleep(1000 * 70)
  system.shutdown()

}
