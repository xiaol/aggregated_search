package utils

// Created by ZG on 15/9/10.
// 

import scala.util.control.ControlThrowable

object ExceptionHandler {
  def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
    case ex: ControlThrowable => throw ex

    // case ex: OutOfMemoryError (Assorted other nasty exceptions you don't want to catch)

    //If it's an exception they handle, pass it on
    case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)

    // If they didn't handle it, rethrow. This line isn't necessary, just for clarity
    case ex: Throwable => throw ex
  }
}

// Usage:
/*
def doSomething: Unit = {
  try {
    somethingDangerous
  } catch safely {
    ex: Throwable => println("AHHH")
  }
}
*/