package routers

// Created by ZG on 15/7/20.
//

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App{

  // load config from application.conf
  val config = ConfigFactory.load()

  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  // ..++ load other config like db

  // create an ActorSystem name `my-spray` to host our application in
  implicit val system = ActorSystem("root-service")

  // create and start our service actor
  //  val service = system.actorOf(Props[MyServiceActor], "demo-service")   // default service of template
  val service = system.actorOf(Props[RestInterface], "search-service")

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http).ask(Http.Bind(service, interface = host, port = port))
    .mapTo[Http.Event]
    .map {
    case Http.Bound(address) =>
      println(s"REST interface bound to $address")
    case Http.CommandFailed(cmd) =>
      println("REST interface could not bind to " +
        s"$host:$port, ${cmd.failureMessage}")
      system.shutdown()
  }

}
