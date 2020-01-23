package ch.taggiasco.streams

import scala.util.matching.Regex


object Reducer {
  
  private val NOT_PERTINENT = "not pertinent"
  
  
  val identity: LogEntry => Particularity = logEntry => {
    IdentityParticularity()
  }
  
  
  val httpMethod: LogEntry => Particularity = logEntry => SingleParticularity(logEntry.httpMethod)
  
  
  val status: LogEntry => Particularity = logEntry => SingleParticularity(logEntry.status.toString())
  
  
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
  
  
  val directPathOnly: LogEntry => Particularity = logEntry => {
    val index = logEntry.url.indexOf("?")
    if(index < 0) {
      SingleParticularity(logEntry.url)
    } else {
      SingleParticularity(logEntry.url.substring(0, index))
    }
  }
  
  
  private def extractPathAndArgument(logEntry: LogEntry, argument: String): String = {
    logEntry.url match {
      case SimpleURL(path, queryString) =>
        queryString.toUpperCase().split("&").find(s => s.startsWith(argument.toUpperCase())) match {
          case Some(argElement) =>
            path + " / " + argElement.toLowerCase()
          case None =>
            NOT_PERTINENT
        }
      case FullURL(path) => NOT_PERTINENT
    }
  }
  
  
  def byArgument(argument: String): LogEntry => Particularity = logEntry => {
    SingleParticularity(extractPathAndArgument(logEntry, argument))
  }
  
  
  def statusAndByArgument(argument: String): LogEntry => Particularity = logEntry => {
    SingleParticularity(extractPathAndArgument(logEntry, argument) + " / " + logEntry.status.toString())
  }
  
  
  val dateHour: LogEntry => Particularity = logEntry => {
    val datas = logEntry.date.split(" ").toList
    DualParticularity(datas(0), datas(1).split(":").head)
  }
  
  
}