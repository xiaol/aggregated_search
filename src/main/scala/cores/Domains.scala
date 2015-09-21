package cores

// Created by ZG on 15/9/9.
// 

/*
  Messages for http response.
  Any case class would use as response should extends trait RestMessage.
 */
trait RestMessage
case class Error(message:String)
case class Validation(messgae:String)
case class ResponseMessage(key:String) extends RestMessage