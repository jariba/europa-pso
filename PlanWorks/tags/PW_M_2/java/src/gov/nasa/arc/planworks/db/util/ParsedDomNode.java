package gov.nasa.arc.planworks.db.util;

public class ParsedDomNode {
  
  private Short nodeType;
  private String nodeName;
  private String nodeValue;
  
  public ParsedDomNode( Short nodeType, String nodeName, String nodeValue) {
    this.nodeType = nodeType;
    this.nodeName = nodeName;
    this.nodeValue = nodeValue;
  } // end constructor
  
  
  /**
   * Gets the value of nodeType
   *
   * @return the value of nodeType
   */
    public Short getNodeType()  {
      return this.nodeType;
    }
  
  /**
   * Gets the value of nodeName
   *
   * @return the value of nodeName
   */
  public String getNodeName()  {
    return this.nodeName;
    }
  
  /**
   * Gets the value of nodeValue
   *
   * @return the value of nodeValue
   */
  public String getNodeValue()  {
      return this.nodeValue;
  }
  
} // end class ParsedDomNode


