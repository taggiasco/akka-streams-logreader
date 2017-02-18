package ch.taggiasco.streams


object Reducer {
  
  val httpMethodReducer: LogEntry => Particularity = logEntry => {
    Particularity(logEntry.httpMethod)
  }
  
  private val SimpleURL = "(.+)\\?(.*)".r
  private val FullURL   = "(.+)".r
  
  val urlReducer: LogEntry => Particularity = logEntry => {
    Particularity(
      logEntry.url match {
        case SimpleURL(path, queryString) => path
        case FullURL(path) => path
      }
    )
  }
  
}