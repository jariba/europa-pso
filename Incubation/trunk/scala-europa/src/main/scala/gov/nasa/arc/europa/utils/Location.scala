package gov.nasa.arc.europa.utils

object SourceLocation { 
  def apply(callsUp: Int = 0): StackTraceElement = {
    if(callsUp + 1 < 0)
      return null;
    try {
      throw new Exception();
    }
    catch {
      case e: Exception => {
	var stack = e.getStackTrace
	if(callsUp + 1 < stack.length)
	  return stack(callsUp + 1)
	return null
      }
    }
  }

}
