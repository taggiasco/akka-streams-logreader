package ch.taggiasco.streams

object TimingGroup {
  
  val defaultGroups = Array(800, 1200)
  
  
  def group(groups: Array[(Int, Int)], entry: LogEntry): Int = {
    groups.find(e => {
      entry.timing > e._1 && (entry.timing <= e._2 || e._2 == 0)
    }).map(_._1).getOrElse(-1)
  }
  
  
  def getLabel(groups: Array[(Int, Int)], n: Int): String = {
    groups.find(e => e._1 == n).map(e => {
      s"""Between ${e._1} and ${if(e._2 == 0) { "above" } else { e._2 }}"""
    }).getOrElse("Undefined")
  }
  
  
}