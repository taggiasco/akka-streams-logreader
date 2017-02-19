package ch.taggiasco.streams

import java.nio.file.Paths

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
    
    val LogPattern = """(.*) Timing: ([A-Z]+) (.+) took ([0-9]+)ms and returned ([0-9]+)""".r
    
    // read lines from a log file
    if(args.isEmpty) {
      println("You must add filenames as arguments")
      system.terminate()
    }
    val logFiles = args.map(arg => Paths.get("src/main/resources/" + arg))
    
    val sources: Array[Source[String, Future[IOResult]]] = logFiles.map(logFile => {
      FileIO.fromPath(logFile).
      via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true)).
      map(_.utf8String)
    })
    
    val source = sources.foldLeft(Source.empty[String])((acc, current) => Source.combine(acc, current)(Merge(_)) )
    
    
    val logEntryFlow: Flow[String, (LogEntry), NotUsed] = {
      Flow[String].collect {
        case line @ LogPattern(date, httpMethod, url, timing, status) =>
          LogEntry(date, httpMethod, url, Integer.parseInt(timing), Integer.parseInt(status), line)
      }
    }
    
    
    def reduceFlow(f: LogEntry => Particularity): Flow[LogEntry, (Particularity, LogEntry), NotUsed] = {
      Flow[LogEntry].map(logEntry => (f(logEntry), logEntry))
    }
    
    
    val sumSink: Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
      Sink.fold(Map.empty[Particularity, Int])(
        (acc: Map[Particularity, Int], dataLogEntry: (Particularity, LogEntry)) => {
          val (data, logEntry) = dataLogEntry
          val current = acc.get(data).getOrElse(0)
          acc + ((data, current + 1))
        }
      )
    }
    
    val avgSink: Sink[(Particularity, LogEntry), Future[Map[Particularity, Int]]] = {
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
    
    
    // graph : count number of requests for each http method
    val graph = source.via(logEntryFlow).via(reduceFlow(Reducer.httpMethodReducer)).runWith(sumSink)
    
    // graph : average response time for each url
    //val graph = source.via(logEntryFlow).via(reduceFlow(Reducer.urlReducer)).runWith(avgSink)
    
    graph.onComplete {
      case Success(results) =>
        println("Results:")
        results.foreach(result => {
          val (particularity, value) = result
          println(s"${particularity.label} : $value")
        })
        system.terminate()
      case Failure(e) =>
        println(s"Failure: ${e.getMessage}")
        system.terminate()
    }
  }
}