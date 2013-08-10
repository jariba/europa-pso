package gov.nasa.arc.europa.utils

sealed class DefaultsTo[A,B]
trait LowPriorityDefaultsTo { 
  implicit def overrideDefault[A, B] = new DefaultsTo[A, B]
}
object DefaultsTo extends LowPriorityDefaultsTo { 
  implicit def default[B] = new DefaultsTo[B, B]
}
class Has[B] { 
  type AsDefault[A] = A DefaultsTo B
}

private class ErrorMessage[E <: Throwable : Has[Exception]#AsDefault : Manifest](fileName: String,
                                                                    lineNumber: Int, message: Any,
                                                                    rest: Any*) { 
  import DefaultsTo._
  def handleAssert = { 
    val fullMessage = new StringBuilder
    fullMessage.append(fileName).append(":").append(lineNumber).append(": ").append(message.toString).append(rest.map(_.toString).mkString)
    if(Error.displayErrors)
      Console.println(fullMessage)
    if(Error.throwExceptions) { 
      val exp = implicitly[Manifest[E]].runtimeClass.getConstructor(classOf[String]).newInstance(fullMessage.toString).asInstanceOf[E]
      throw exp
    }
  }
}

object Error { 
  private[utils] var throwExceptions = true;
  private[utils] var displayWarnings = true;
  private[utils] var displayErrors = true;

  def ALWAYS_FAIL = () => false
  def checkError[E <: Exception : Has[Exception]#AsDefault : Manifest](cond: Boolean, message: Any, rest: Any*): Unit = { 
    if(!cond) { 
      val ste = SourceLocation(1)
      new ErrorMessage[E](ste.getFileName, ste.getLineNumber, message.toString, rest:_*).handleAssert
    }
  }
  def checkError[E <: Exception : Has[Exception]#AsDefault : Manifest](cond: () => Boolean, message: Any, rest: Any*): Unit = { 
    if(!cond()) { 
      val ste = SourceLocation(1)
      new ErrorMessage[E](ste.getFileName, ste.getLineNumber, message.toString, rest:_*).handleAssert
    }
  }

  def doDisplayWarnings = displayWarnings = true
  def doNotDisplayWarnings = displayWarnings = false

  def doDisplayErrors = displayErrors = true
  def doNotDisplayErrors = displayErrors = false

  def doThrowExceptions = throwExceptions = true
  def doNotThrowExceptions = throwExceptions = false

  
}
