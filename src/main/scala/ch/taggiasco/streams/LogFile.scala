package ch.taggiasco.streams

import akka.NotUsed
import akka.util.ByteString
import akka.stream.IOResult
import akka.stream.scaladsl.{Source, FileIO, Framing, Merge}
import scala.concurrent.Future
import java.io.File
import java.nio.file.{Path, Paths}


object LogFile {
  
  private def fromPaths(paths: Array[Path]): Source[String, NotUsed] = {
    val sources: Array[Source[String, Future[IOResult]]] = paths.map(logFile => {
      FileIO.fromPath(logFile).
        via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 10000, allowTruncation = true)).
        map(_.utf8String)
    })
    sources.foldLeft(Source.empty[String])((acc, current) => Source.combine(acc, current)(Merge(_)) )
  }
  
  
  def fromFolder(folder: String): Source[String, NotUsed] = {
    val logFiles = new File(folder).list().map( fname => Paths.get(folder + "/" + fname) )
    fromPaths(logFiles)
  }
  
  
  def fromFile(name: String): Source[String, NotUsed] = {
    fromPaths(Array(Paths.get(name)))
  }
  
}
