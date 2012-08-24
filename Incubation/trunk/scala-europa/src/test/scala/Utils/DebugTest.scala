package gov.nasa.arc.europa.utils.test

import gov.nasa.arc.europa.utils.Debug
import gov.nasa.arc.europa.utils.Debug._
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.collection.immutable.Map

class DebugTest extends FunSuite with ShouldMatchers { 
  def runDebugTest(i: Int, output: PrintWriter): Unit = {
    val resourceName = "/debug" + i + ".cfg"
    val configURL = getClass.getResource(resourceName)
    val configName = configURL.getFile
    Debug.setOutput(output)
    Debug.loadFile(configName)
    debugMsg("main1", "done opening files")
    debugMsg("main1", "std::cout is good")
    debugStmt("main2a", () => output.write("Sum is " + (0 to 5).reduceLeft(_+_) ))
    debugMsg("main2", "primary testing done")
  }

  test("debug test") { 
    val testData: Map[Int, List[String]] = Map(1 -> List("[main1] done opening files", 
                                                         "[main1] std::cout is good"),
                                               3 -> List("[main1] done opening files", 
                                                         "[main1] std::cout is good",
                                                         "[main2a] Sum is 15",
                                                         "[main2] primary testing done"))
    for(i <- 1 to 6) { 
      Debug.clearEnabled
      val writer = new StringWriter
      val tempWriter = new PrintWriter(new BufferedWriter(writer))
      runDebugTest(i, tempWriter)
      tempWriter.close
      val lines = writer.toString.split('\n').toList
      testData.get(i) match {
        case Some(result) => assert(lines === result)
        case _ => lines should be (List(""))
      }
    }
  }
}
