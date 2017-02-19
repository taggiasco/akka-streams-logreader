package ch.taggiasco.streams

import scala.util.matching.Regex


object Reducer {
  
  val httpMethod: LogEntry => Particularity = logEntry => {
    Particularity(logEntry.httpMethod)
  }
  
  private val SimpleURL = "(.+)\\?(.*)".r
  private val FullURL   = "(.+)".r
  
  val pathOnly: LogEntry => Particularity = logEntry => {
    Particularity(
      logEntry.url match {
        case SimpleURL(path, queryString) => path
        case FullURL(path) => path
      }
    )
  }
  
  
}