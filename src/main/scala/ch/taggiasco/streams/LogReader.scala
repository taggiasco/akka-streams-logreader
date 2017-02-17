package ch.taggiasco.streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.util.{Try, Failure, Success}
import akka.NotUsed
import scala.concurrent.Future
import akka.stream.IOResult


object LogReader {

  def main(args: Array[String]): Unit = {
    // actor system and implicit materializer
    implicit val system = ActorSystem("system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = materializer.executionContext
    
    val LogPattern = """(.*) Timing: ([A-Z]+) (.+) took ([0-9]+)ms and returned ([0-9]+)""".r
    
    // get log levels from arguments, or default
    val levels = Try {
      args(1).split("-").map(Integer.parseInt)
    }.toOption.getOrElse(TimingGroup.defaultGroups)
    val groups = (0 +: levels).zipAll( (levels :+ 0), -1, -1 )
    // functions
    val groupCreator: (LogEntry => Int) = TimingGroup.group(groups, _)
    val groupLabel: (Int => String) = TimingGroup.getLabel(groups, _)
    
    // read lines from a log file
    val logFile = Paths.get("src/main/resources/" + args(0))

    FileIO.fromPath(logFile).
      // parse chunks of bytes into lines
      via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true)).
      map(_.utf8String).
      collect {
        case line @ LogPattern(date, httpMethod, url, timing, status) =>
          LogEntry(date, httpMethod, url, Integer.parseInt(timing), Integer.parseInt(status), line)
      }.
      // group them by log level
      groupBy(levels.size + 1, groupCreator).
      fold((0, List.empty[LogEntry])) {
        case ((_, list), logEntry) => (groupCreator(logEntry), logEntry :: list)
      }.
      // write lines of each group to a separate file
      mapAsync(parallelism = levels.size + 1) {
        case (level, groupList) =>
          println(groupLabel(level) + " : " + groupList.size + " requests")
          Source(groupList.reverse).map(entry => ByteString(entry.line + "\n")).runWith(FileIO.toPath(Paths.get(s"target/log-${groupLabel(level)}.txt")))
      }.
      mergeSubstreams.
      runWith(Sink.onComplete {
        case Success(_) =>
          system.terminate()
        case Failure(e) =>
          println(s"Failure: ${e.getMessage}")
          system.terminate()
      })
    
    
    
    
    
    val httpMethodReducer: LogEntry => Particularity = logEntry => {
      Particularity(logEntry.httpMethod)
    }
    
    
    val source: Source[String, Future[IOResult]] =
      FileIO.fromPath(logFile).
      via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true)).
      map(_.utf8String)
    
    
    val logEntryFlow: Flow[String, (LogEntry), NotUsed] = {
      Flow[String].collect {
        case line @ LogPattern(date, httpMethod, url, timing, status) =>
          LogEntry(date, httpMethod, url, Integer.parseInt(timing), Integer.parseInt(status), line)
      }
    }
    
    
    def reduceFlow(f: LogEntry => Particularity): Flow[LogEntry, (Particularity, LogEntry), NotUsed] = {
      Flow[LogEntry].map(logEntry => (f(logEntry), logEntry))
    }
    
    
    val counterSink: Sink[(Particularity,LogEntry), Future[Map[Particularity, Int]]] = {
      Sink.fold(Map.empty[Particularity, Int])(
        (acc: Map[Particularity, Int], dataLogEntry: (Particularity, LogEntry)) => {
          val (data, logEntry) = dataLogEntry
          val current = acc.get(data).getOrElse(0)
          acc + ((data, current + 1))
        }
      )
    }
    
    
    val graph = source.via(logEntryFlow).via(reduceFlow(httpMethodReducer)).runWith(counterSink)
    
    graph.onComplete {
      case Success(_) =>
        system.terminate()
      case Failure(e) =>
        println(s"Failure: ${e.getMessage}")
        system.terminate()
    }
  }
}