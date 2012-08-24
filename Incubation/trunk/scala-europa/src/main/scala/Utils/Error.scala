package gov.nasa.arc.europa.utils


private class ErrorMessage[E <: Exception](fileName: String, lineNumber: Int, message: Any,
                                                       rest: Any*) { 
  def handleAssert(implicit m: Manifest[E]) = { 
    val fullMessage = new StringBuilder
    fullMessage.append(fileName).append(":").append(lineNumber).append(": ").append(message.toString).append(rest.map(_.toString))
    if(Error.displayErrors)
      Console.println(fullMessage)
    if(Error.throwExceptions)
      throw m.erasure.getConstructor(classOf[String]).newInstance(fullMessage.toString).asInstanceOf[E]
  }
}

object Error { 
  private[utils] var throwExceptions = true;
  private[utils] var displayWarnings = true;
  private[utils] var displayErrors = true;

  def ALWAYS_FAIL = () => false
  def checkError[E <: Exception](cond: Boolean, message: Any, rest: Any*)(implicit m: Manifest[E]): Unit = { 
    if(!cond) { 
      val ste = SourceLocation(1)
      new ErrorMessage[E](ste.getFileName, ste.getLineNumber, message.toString, rest:_*).handleAssert(m)
    }
  }
  def checkError[E <: Exception](cond: () => Boolean, message: Any, rest: Any*)(implicit m: Manifest[E]): Unit = { 
    checkError(cond(), message, rest)(m)
  }

  def doDisplayWarnings = displayWarnings = true
  def doNotDisplayWarnings = displayWarnings = false

  def doDisplayErrors = displayErrors = true
  def doNotDisplayErrors = displayErrors = false

  def doThrowExceptions = throwExceptions = true
  def doNotThrowExceptions = throwExceptions = false

  
}
