package gov.nasa.arc.europa.utils;
import java.io.StringWriter

import scala.io.Source
import collection.immutable.HashSet

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.PrintWriter
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object Debug {
  private var output: PrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
  private var enabledMsgs: Set[String] = HashSet.empty
  var allEnabled = false
  var disabled = false

  def loadFile(in: Source): Unit = { 
    for(rawLine <- in.getLines) {
      val line = rawLine.split("#", 2)(0)
      if(!line.matches("^\\s*$"))
	enable(line)
    }
  }
  def loadFile(fileName: String): Unit = loadFile(Source.fromInputStream(new BufferedInputStream(new FileInputStream(fileName))))

  def enableAll: Unit = { allEnabled = true; disabled = false;}
  def disableAll: Unit = { allEnabled = false; disabled = true;}
  def clearEnabled: Unit = enabledMsgs = Set.empty
  private def enable(marker: String): Unit = {
    enabledMsgs = enabledMsgs + marker.replaceFirst("\\s*:", "");
    disabled = false;
  }

  private def isEnabled(marker: String) = { 
    !disabled && (allEnabled || marker == null || enabledMsgs.exists(m => marker.indexOf(m) != -1))
  }

  def setOutput(o: PrintWriter): Unit = output = o
  def getOutput: PrintWriter = output

  private def printMarker(marker: String) = {
    if(marker != null) {
      Debug.output.print("[")
      Debug.output.print(marker)
      Debug.output.print("] ")
    }
  }

  def debugMsg(marker: String, first: Any): Unit = {
    if(Debug.isEnabled(marker)) {
      printMarker(marker)
      Debug.output.print(first)
      Debug.output.println
      Debug.output.flush
    }
  }

  def debugMsg(marker: String, first: Any, rest: Any*): Unit = {
    if(Debug.isEnabled(marker)) {
      printMarker(marker)
      Debug.output.print(first);
      for(r <- rest)
	Debug.output.print(r)
      Debug.output.println
      Debug.output.flush
    }
  }

  def debugMsg(marker: String): Unit = {
    if(Debug.isEnabled(marker)) {
      val ste = SourceLocation(1)
      if(ste != null)
	debugMsg(marker, ste.getFileName, ":", ste.getLineNumber)
      Debug.output.flush
    }
  }

  def debugStmt(marker: String, stmt: () => Unit): Unit = { 
    if(Debug.isEnabled(marker)) { 
      printMarker(marker)
      stmt()
      Debug.output.println
      Debug.output.flush
    }
  }

  def condDebugStmt(marker: String, cond: () => Boolean, stmt: () => Unit): Unit = { 
    if(Debug.isEnabled(marker) && cond()) { stmt()}
  }

  def condDebugMsg(cond: () => Boolean, marker: String, first: Any): Unit = {
    if(Debug.isEnabled(marker) && cond()) {
      printMarker(marker)
      Debug.output.print(first)
      Debug.output.println
      Debug.output.flush
    }
  }

  def condDebugMsg(cond: () => Boolean, marker: String, first: Any, rest: Any*): Unit = {
    if(Debug.isEnabled(marker) && cond()) {
      printMarker(marker)
      Debug.output.print(first);
      for(r <- rest)
	Debug.output.print(r)
      Debug.output.println
      Debug.output.flush
    }
  }

  def condDebugMsg(cond: () => Boolean, marker: String): Unit = {
    if(Debug.isEnabled(marker) && cond()) {
      val ste = SourceLocation(1)
      if(ste != null)
	debugMsg(marker, ste.getFileName, ":", ste.getLineNumber)
      Debug.output.flush
    }
  }
}
