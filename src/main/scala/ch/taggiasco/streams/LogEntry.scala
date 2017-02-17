package ch.taggiasco.streams

case class LogEntry(
  date:       String,
  httpMethod: String,
  url:        String,
  timing:     Int,
  status:     Int,
  line:       String
)

case class Particularity(
  label: String
)
