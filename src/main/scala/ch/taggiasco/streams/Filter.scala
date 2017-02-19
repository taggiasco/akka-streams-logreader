package ch.taggiasco.streams

import scala.util.matching.Regex


object Filter {
  
  def httpMethod(method: String): LogEntry => Boolean = logEntry => {
    logEntry.httpMethod.toUpperCase() == method.toUpperCase()
  }
  
  def pathPattern(pattern: Regex): LogEntry => Boolean = logEntry => {
    logEntry.url match {
      case pattern() => true
      case _ => false
    }
  }
  
  val useless: LogEntry => Boolean = logEntry => true
  
}