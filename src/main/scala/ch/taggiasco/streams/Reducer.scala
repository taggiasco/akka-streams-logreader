package ch.taggiasco.streams

import scala.util.matching.Regex


object Reducer {
  
  val identity: LogEntry => Particularity = logEntry => {
    IdentityParticularity()
  }
  
  val httpMethod: LogEntry => Particularity = logEntry => {
    SingleParticularity(logEntry.httpMethod)
  }
  
  
  private val SimpleURL = "(.+)\\?(.*)".r
  private val FullURL   = "(.+)".r
  
  val pathOnly: LogEntry => Particularity = logEntry => {
    SingleParticularity(
      logEntry.url match {
        case SimpleURL(path, queryString) => path
        case FullURL(path) => path
      }
    )
  }
  
  
  val dateHour: LogEntry => Particularity = logEntry => {
    DualParticularity("", "")
  }
  
  
}