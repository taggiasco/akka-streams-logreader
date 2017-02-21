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
  
  
  def percentile(value: Int): Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
    ???
  }
  
}
