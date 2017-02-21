package ch.taggiasco.streams

import akka.stream.scaladsl.Flow
import akka.NotUsed

case class LogEntry(
  date:       String,
  httpMethod: String,
  url:        String,
  timing:     Int,
  status:     Int,
  line:       String
)


object LogEntry {
  
  val LogPattern = """(.*) Timing: ([A-Z]+) (.+) took ([0-9]+)ms and returned ([0-9]+)""".r
  
  val flow: Flow[String, LogEntry, NotUsed] = {
    Flow[String].collect {
      case line @ LogPattern(date, httpMethod, url, timing, status) =>
        LogEntry(date, httpMethod, url, Integer.parseInt(timing), Integer.parseInt(status), line)
    }
  }
  
}
