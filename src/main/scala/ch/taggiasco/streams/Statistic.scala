package ch.taggiasco.streams

import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import scala.concurrent.ExecutionContext


object Statistic {
  
  val sumSink: Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
    Sink.fold(Map.empty[Particularity, Int])(
      (acc: Map[Particularity, Int], dataLogEntry: (Particularity, LogEntry)) => {
        val (data, logEntry) = dataLogEntry
        val current = acc.get(data).getOrElse(0)
        acc + ((data, current + 1))
      }
    )
  }
  
  
  def avgSink()(implicit execution: ExecutionContext): Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
    val sink = Sink.fold(Map.empty[Particularity, (Int, Int)])(
      (acc: Map[Particularity, (Int, Int)], dataLogEntry: (Particularity, LogEntry)) => {
        val (data, logEntry) = dataLogEntry
        val (current, value) = acc.get(data).getOrElse((0, 0))
        acc + ((data, (current + 1, value + logEntry.timing)))
      }
    )
    sink.mapMaterializedValue(
      (value: Future[Map[Particularity, (Int, Int)]]) => {
        value.map(vs => vs.map(v => v._1 -> v._2._2 / v._2._1))
      }
    )
  }
  
  
  val worseSink: Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
    Sink.fold(Map.empty[Particularity, Int])(
      (acc: Map[Particularity, Int], dataLogEntry: (Particularity, LogEntry)) => {
        val (data, logEntry) = dataLogEntry
        val current = acc.get(data).getOrElse(0)
        val max = Math.max(current, logEntry.timing)
        acc + ((data, max))
      }
    )
  }
  
  
  def nWorsesSink(max: Int): Sink[(Particularity, LogEntry), Future[Map[Particularity, List[Int]]]] = {
    Sink.fold(Map.empty[Particularity, List[Int]])(
      (acc: Map[Particularity, List[Int]], dataLogEntry: (Particularity, LogEntry)) => {
        val (data, logEntry) = dataLogEntry
        val current = acc.get(data).getOrElse(Nil)
        val values = current :+ logEntry.timing
        if(values.size <= max) {
          acc + ((data, values))
        } else {
          acc + ((data, values.sorted.drop(1)))
        }
        
      }
    )
  }
  
  
  private def percentile(values: List[Int], index: Int): Int = {
    val sortedValues = values.sorted
    val pos = Math.ceil( (sortedValues.length - 1) * (index / 100.0)).toInt
    sortedValues(pos)
  }
  
  
  def percentile(index: Int)(implicit execution: ExecutionContext): Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
    // non optimal version !!!
    val sink = Sink.fold(Map.empty[Particularity, List[Int]])(
      (acc: Map[Particularity, List[Int]], dataLogEntry: (Particularity, LogEntry)) => {
        val (data, logEntry) = dataLogEntry
        val current = acc.get(data).getOrElse(Nil)
        acc + ((data, current :+ logEntry.timing))
      }
    )
    sink.mapMaterializedValue(
      (value: Future[Map[Particularity, List[Int]]]) => {
        value.map(vs => vs.map(v => v._1 -> percentile(v._2, index)))
      }
    )
  }
  
}
