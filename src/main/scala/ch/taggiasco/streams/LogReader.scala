package ch.taggiasco.streams

import java.nio.file.Paths
import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.util.{Try, Failure, Success}
import scala.concurrent.Future


object LogReader {

  def main(args: Array[String]): Unit = {
    // actor system and implicit materializer
    implicit val system = ActorSystem("system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = materializer.executionContext
    
    // read lines from log files
    val source = {
      if(args.isEmpty) {
        LogFile.fromFolder("src/main/resources")
      } else {
        LogFile.fromFile(args(0))
      }
    }
    
    
    def reduceFlow(f: LogEntry => Particularity): Flow[LogEntry, (Particularity, LogEntry), NotUsed] = {
      Flow[LogEntry].map(logEntry => (f(logEntry), logEntry))
    }
    
    def filterFlow(f: LogEntry => Boolean): Flow[LogEntry, LogEntry, NotUsed] = Flow[LogEntry].filter(f)
    
    
    
    // graph : count number of requests for each http method
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.useless)).via(reduceFlow(Reducer.httpMethod)).runWith(Statistic.sumSink)
    
    // graph : average response time for each url
    //val graph = source.via(LogEntry.flow).via(reduceFlow(Reducer.pathOnly)).runWith(Statistic.avgSink())
    
    // graph : count number of requests per day/hour
    //val graph = source.via(LogEntry.flow).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.sumSink)
    
    // graph : number of request for each status
    //val graph = source.via(LogEntry.flow).via(reduceFlow(Reducer.status)).runWith(Statistic.sumSink)
    
    // graphs for services
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.status)).runWith(Statistic.sumSink)
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.sumSink)
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.avgSink)
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.percentile(95))
    //val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.worseSink)
    val graph = source.via(LogEntry.flow).via(filterFlow(Filter.pathPattern("\\Sservice/horaires\\S".r))).via(reduceFlow(Reducer.dateHour)).runWith(Statistic.nWorsesSink(5))
    
    graph.onComplete {
      case Success(results) =>
        println("Results:")
        val sortedKeys = results.keys.toList.sortBy(_.label)
        sortedKeys.foreach(particularity => {
          val value = results(particularity)
          println(s"${particularity.label} : ${value.sorted.mkString(", ")}")
        })
        system.terminate()
      case Failure(e) =>
        println(s"Failure: ${e.getMessage}")
        system.terminate()
    }
  }
}