package ch.taggiasco.streams

import scala.util.matching.Regex


object Filter {
  
  def httpMethod(method: String): LogEntry => Boolean = logEntry => {
    logEntry.httpMethod.toUpperCase() == method.toUpperCase()
  }
  
  def pathPattern(pattern: Regex): LogEntry => Boolean = logEntry => {
    pattern.findFirstIn(logEntry.url).isDefined
  }
  
  val useless: LogEntry => Boolean = logEntry => true
  
}