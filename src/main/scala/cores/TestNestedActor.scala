package cores

// Created by ZG on 15/9/14.
//


import akka.actor.{Props, Actor, ActorRef}
import com.search

class TestNestedActor(testNestedSubActor:ActorRef) extends Actor{

  def receive = {
    case searchWithKey:search.SearchWithKey =>
      println("searchWithKey")
      testNestedSubActor ! search.Error("go on running")
      self ! search.ResponseMessage("response")

    case responseMessage:search.ResponseMessage =>
      println("get responseMessage")
      context.parent ! search.ResponseMessage("response")

    case _ => println("wrong")
  }

}

class TestNestedSubActor extends Actor{

  def receive = {
    case err:search.Error =>
      println("get error in TestNestedSubActor")
      context.actorOf(Props(new Actor() {

        def receive = {
          case x:String =>
            println(s"In inner actor, get: $x")
            if(x=="test511") context.stop(self)
          case _ =>
        }

        self ! "test1"
                Thread.sleep(1000 * 2)
        self ! "test2"
                Thread.sleep(1000 * 2)
        self ! "test3"
                Thread.sleep(1000 * 2)
        self ! "test4"
                Thread.sleep(1000 * 2)
        self ! "test5"
//        for(x <- 1 to 1000)
//          self ! s"x"
        self ! "test511"

      }))
  }

}
