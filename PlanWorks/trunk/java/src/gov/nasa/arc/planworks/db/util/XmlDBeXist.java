// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: XmlDBeXist.java,v 1.3 2003-05-15 22:16:23 taylor Exp $
//
// XmlDBeXist - XML data base interface thru XML:DB API to
//              eXist-0.9 db server
//
// Will Taylor -- started 03april03
//

// add to compile classpath
// /home/wtaylor/pub/eXist-0.9/exist.jar
// /home/wtaylor/pub/eXist-0.9/lib/core/xmldb.jar

package gov.nasa.arc.planworks.db.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// PlanWorks/java/lib/eXist/exist.jar
import org.exist.xmldb.LocalXMLResource;

//  PlanWorks/java/lib/eXist/lib/core/xmldb.jar
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;


/**
 * <code>XmlDBeXist</code> - XML data base interface thru XML:DB API to
 *                           eXist-0.9 db server
 *                           implements planworks.db.XmlDBAccess interface
 *                           as a singleton class
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                     NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class XmlDBeXist {

  private static final String DRIVER_NAME = "org.exist.xmldb.DatabaseImpl";
  private static final String URI = "xmldb:exist://";
  private static final String ROOT_COLLECTION_NAME = "/db";
  // "guest" & "guest"
  private static final String USER = org.exist.security.SecurityManager.GUEST_USER;
  private static final String PASSWORD = org.exist.security.SecurityManager.GUEST_USER;
  private static final short END_ELEMENT_NODE = 99;

  // should match DTD
  private static final String PARTIAL_PLAN_ELEMENT = "PartialPlan";
  private static final String PARTIAL_PLAN_KEY_ATTRIBUTE = "key";
  private static final String PARTIAL_PLAN_MODEL_ATTRIBUTE = "model";
  private static final String OBJECT_ELEMENT = "Object";
  private static final String OBJECT_KEY_ATTRIBUTE = "key";
  private static final String OBJECT_NAME_ATTRIBUTE = "name";
  private static final String TIMELINE_ELEMENT = "Timeline";
  private static final String TIMELINE_KEY_ATTRIBUTE = "key";
  private static final String TIMELINE_NAME_ATTRIBUTE = "name";
  private static final String SLOT_ELEMENT = "Slot";
  private static final String SLOT_KEY_ATTRIBUTE = "key";
  private static final String TOKEN_ELEMENT = "Token";
  private static final String TOKEN_KEY_ATTRIBUTE = "key";
  private static final String TOKEN_IS_VALUE_TOKEN_ATTRIBUTE = "isValueToken";
  private static final String TOKEN_PREDICATE_ID_ATTRIBUTE = "predicateId";
  private static final String TOKEN_START_VAR_ID_ATTRIBUTE = "startVarId";
  private static final String TOKEN_END_VAR_ID_ATTRIBUTE = "endVarId";
  private static final String TOKEN_DURATION_VAR_ID_ATTRIBUTE = "durationVarId";
  private static final String TOKEN_OBJECT_VAR_ID_ATTRIBUTE = "objectVarId";
  private static final String TOKEN_REJECT_VAR_ID_ATTRIBUTE = "rejectVarId";
  private static final String TOKEN_TOKEN_RELATION_IDS_ATTRIBUTE = "tokenRelationIds";
  private static final String TOKEN_PARAM_VAR_IDS_ATTRIBUTE = "paramVarIds";
  private static final String TOKEN_SLOT_ID_ATTRIBUTE = "slotId";
  private static final String VARIABLE_ELEMENT = "Variable";
  private static final String VARIABLE_KEY_ATTRIBUTE = "key";
  private static final String VARIABLE_TYPE_ATTRIBUTE = "type";
  private static final String VARIABLE_CONSTRAINT_IDS_ATTRIBUTE = "constraintIds";
  private static final String VARIABLE_PARAM_ID_ATTRIBUTE = "paramId";
  private static final String ENUMERATED_DOMAIN_ELEMENT = "EnumeratedDomain";
  private static final String INTERVAL_DOMAIN_ELEMENT = "IntervalDomain";
  private static final String INTERVAL_DOMAIN_TYPE_ATTRIBUTE = "type";
  private static final String INTERVAL_DOMAIN_LOWER_BOUND_ATTRIBUTE = "lowerBound";
  private static final String INTERVAL_DOMAIN_UPPER_BOUND_ATTRIBUTE = "upperBound";
  private static final String PREDICATE_ELEMENT = "Predicate";
  private static final String PREDICATE_KEY_ATTRIBUTE = "key";
  private static final String PREDICATE_NAME_ATTRIBUTE = "name";
  private static final String PARAMETER_ELEMENT = "Parameter";
  private static final String PARAMETER_KEY_ATTRIBUTE = "key";
  private static final String PARAMETER_NAME_ATTRIBUTE = "name";

  /**
   * constant <code>NUM_TOKEN_ATTRIBUTES</code>
   *
   */
  public static final int NUM_TOKEN_ATTRIBUTES = 11;

  // XML eXist Queries
  private static final String END_SELECT_VALUE = "\"]";
  private static final String MODEL_KEY_QUERY_PRE;
  private static final String MODEL_KEY_QUERY_POST;
  private static final String TIMELINE_KEY_QUERY;
  private static final String VARIABLE_KEY_QUERY;
  private static final String PREDICATE_KEY_QUERY;
  private static final String PARTIAL_PLAN_OBJECT_QUERY;

  static {
    // /PartialPlan[@key="
    StringBuffer xmlQuery = new StringBuffer( "/");
    xmlQuery.append( PARTIAL_PLAN_ELEMENT).append( "[@");
    xmlQuery.append( PARTIAL_PLAN_KEY_ATTRIBUTE).append( "=\"");
    MODEL_KEY_QUERY_PRE = xmlQuery.toString();

    // "]@model
    xmlQuery = new StringBuffer( "\"]@");
    xmlQuery.append( PARTIAL_PLAN_MODEL_ATTRIBUTE);
    MODEL_KEY_QUERY_POST = xmlQuery.toString();

    // /PartialPlan/Object
    xmlQuery = new StringBuffer( "/");
    xmlQuery.append( PARTIAL_PLAN_ELEMENT).append( "/");
    xmlQuery.append( OBJECT_ELEMENT);
    PARTIAL_PLAN_OBJECT_QUERY = xmlQuery.toString();

    // /PartialPlan/Object/Timeline[@key="
    xmlQuery = new StringBuffer( "/");
    xmlQuery.append( PARTIAL_PLAN_ELEMENT).append( "/");
    xmlQuery.append( OBJECT_ELEMENT).append( "/");
    xmlQuery.append( TIMELINE_ELEMENT).append( "[@");
    xmlQuery.append( TIMELINE_KEY_ATTRIBUTE).append( "=\"");
    TIMELINE_KEY_QUERY = xmlQuery.toString();

    // query => /PartialPlan/Variable[@key="
    xmlQuery = new StringBuffer( "/");
    xmlQuery.append( PARTIAL_PLAN_ELEMENT).append( "/");
    xmlQuery.append( VARIABLE_ELEMENT).append( "[@");
    xmlQuery.append( VARIABLE_KEY_ATTRIBUTE).append( "=\"");
    VARIABLE_KEY_QUERY = xmlQuery.toString();

    // query => /PartialPlan/Predicate[@key="
    xmlQuery = new StringBuffer( "/");
    xmlQuery.append( PARTIAL_PLAN_ELEMENT).append( "/");
    xmlQuery.append( PREDICATE_ELEMENT).append( "[@");
    xmlQuery.append( PREDICATE_KEY_ATTRIBUTE).append( "=\"");
    PREDICATE_KEY_QUERY = xmlQuery.toString();
  }

  private static List nodeContentList;
  private static Stack elementNameStack;

  public static final XmlDBeXist INSTANCE = new XmlDBeXist();

  private XmlDBeXist() {
    // singleton class
  }

  /**
   * <code>registerDataBase</code>
   *
   *          to clear data base
   *          rm -f ~/pub/eXist-0.9/webapp/WEB-INF/data/*
   */
  public static void registerDataBase() {
    String xmlDbExcepStr = "XML:DB Exception in registerDataBase";
    Collection collection = null;
    try {
      Class driverClass = Class.forName( DRIVER_NAME);
      Database database = (Database) driverClass.newInstance();
//       System.err.println( "database.getConformanceLevel " +
//                           database.getConformanceLevel());
      // http://www.xml.com/pub/a/2002/01/09/xmldb_api.html
      // says conformance level = 1 provides XPathQueryService
      // org.exist.xmldb.DatabaseImpl provides it, but does not
      // set conformance level = 1 
//       if ( ! database.getConformanceLevel().equals( "1") ) {
//         System.err.println( "This program requires a Core Level 1 XML:DB API driver");
//         System.exit(1);
//       }
      DatabaseManager.registerDatabase( database);
      database.setProperty( "create-database", "true");
        
//       collection =
//         DatabaseManager.getCollection( URI + ROOT_COLLECTION_NAME, USER, PASSWORD);
//       collection.setProperty( "pretty", "true");
//       // collection.setProperty( "encoding", "ISO-8859-1");
//       collection.setProperty( "encoding", "UTF-8");

    } catch (XMLDBException e1) {
      System.err.println( xmlDbExcepStr + "(e1): " + e1.errorCode + " " +
                          e1.getMessage());
      e1.printStackTrace(); 
      System.exit( 1);
    } catch (ClassNotFoundException e2) {
      System.err.println( "ClassNotFoundException in registerDataBase :" +
                         e2);
      e2.printStackTrace(); 
      System.exit( 1);
    } catch (InstantiationException e3) {
      System.err.println( "InstantiationException in registerDataBase :" +
                         e3);
      e3.printStackTrace(); 
      System.exit( 1);
    } catch (IllegalAccessException e4) {
      System.err.println( "IllegalAccessException in registerDataBase :" +
                         e4);
      e4.printStackTrace(); 
      System.exit( 1);
    } finally {
      try {
        if (collection != null) { collection.close(); }
      } catch (XMLDBException e5) {
        System.err.println( xmlDbExcepStr + "(e5): " + e5.errorCode + " " +
                            e5.getMessage());
      e5.printStackTrace();
      System.exit( 1);
      }
    }
  } // end registerDataBase


  /**
   * <code>createCollection</code>
   *
   * @param collectionName - <code>String</code> - 
   * @return collection - <code>Collection</code> -
   */
  public static Collection createCollection( String collectionName) {
    if (collectionName.startsWith( ROOT_COLLECTION_NAME)) {
      // remove /db if specified
      collectionName = collectionName.substring( ROOT_COLLECTION_NAME.length());
    }
    Collection collection = null;
    // try to get collection
    try {
      collection = 
        DatabaseManager.getCollection( URI + ROOT_COLLECTION_NAME + collectionName,
                                       USER, PASSWORD);
      // System.err.println("createCollection: getCollection1 " + collection);
      if (collection == null) {
        // collection does not exist: get root collection and create it
        Collection root = DatabaseManager.getCollection( URI + ROOT_COLLECTION_NAME,
                                                         USER, PASSWORD);
        // System.err.println("createCollection: root " + root);
        CollectionManagementService mgtService = 
          (CollectionManagementService)
          root.getService( "CollectionManagementService", "1.0");
        try {
          // this what http://exist-db.org/devguide.html says to do =>
          // XML:DB Exception occurred: 300 collection not found
          // System.err.println( "create collection " + collectionName);
          // collection = mgtService.createCollection( collectionName);
          // this works
          // collection = mgtService.createCollection( ROOT_COLLECTION_NAME + collectionName);
          String printStr = "Created Collection " + ROOT_COLLECTION_NAME + collectionName;
          System.err.println( printStr);
          collection = mgtService.createCollection( ROOT_COLLECTION_NAME + collectionName);
        } catch (XMLDBException e0) {
          // ignore 300: collection not found exception, and then do getCollection again
          if (e0.errorCode != 300) {
            System.err.println("XML:DB Exception in createCollection0: " +
                               e0.errorCode + " " + e0.getMessage());
            e0.printStackTrace();
            System.exit( 1);
          }
        }
        collection = 
          DatabaseManager.getCollection( URI + ROOT_COLLECTION_NAME + collectionName,
                                         USER, PASSWORD);
      // System.err.println("createCollection: getCollection2 " + collection);
      }
    } catch (XMLDBException e1) {
      System.err.println("XML:DB Exception in createCollection1: " +
                         e1.errorCode + " " + e1.getMessage());
      e1.printStackTrace();
      System.exit( 1);
    }
    // System.err.println("createCollection: collectionName " + collectionName +
    //                    " collection " + collection);
    return collection;
  } // end createCollection

  /**
   * <code>addXMLFileToCollection</code>
   *
   * @param collectionName - <code>String</code> - 
   * @param filePathname - <code>String</code> - 
   */
  public static void addXMLFileToCollection( String collectionName,
                                             String filePathname) {
    // System.err.println("addXMLFileToCollection: collectionName " +
    //                    collectionName + " filePathname " + filePathname);
    // create collection, if needed
    Collection collection = createCollection( collectionName); 
    try {
      // create new XMLResource; an id will be assigned to the new resource
      XMLResource document =
        (XMLResource) collection.createResource( null, "XMLResource");
      File file = new File( filePathname);
      if (! file.canRead()) {
        System.err.println( "addXMLFileToCollection: can't read file " + filePathname);
        return;
      }
      document.setContent( file);
      String printStr2 = "   ... storing in collection " + ROOT_COLLECTION_NAME +
        collectionName;
      System.err.print( printStr2 + " ...");
      collection.storeResource( document);
      System.err.println( "ok.");
    } catch (XMLDBException e1) {
      System.err.println("XML:DB Exception in addXMLFileToCollection: " +
                         e1.errorCode + " " + e1.getMessage());
      System.exit( 1);
    } finally {
      try {
        if (collection != null) { collection.close(); }
      } catch (XMLDBException ex) {
        System.err.println( "addXMLFileToCollection: " + ex.errorCode +
                            " " + ex.getMessage());
        ex.printStackTrace();
      }
    }
  } // end addXMLFileToCollection


  /**
   * <code>queryCollection</code>
   *
   * @param collectionName - <code>String</code> - 
   * @param query - <code>String</code> - 
   * @return nodeContentList - <code>List of ParsedDomNode</code> - 
   */
  public static List queryCollection( String collectionName, String query) {
    if (collectionName.startsWith( ROOT_COLLECTION_NAME)) {
      // remove /db if specified
      collectionName = collectionName.substring( ROOT_COLLECTION_NAME.length());
    }
    Collection collection = null;
    try {
      collection = 
        DatabaseManager.getCollection( URI + ROOT_COLLECTION_NAME + collectionName,
                                       USER, PASSWORD);
      if (collection == null) {
        System.err.println( "queryCollection: collection " + URI +
                            ROOT_COLLECTION_NAME + collectionName + " not found");
        return (new ArrayList());
      }
      System.err.println( "Query: " + query);
      XPathQueryService queryService =
        (XPathQueryService) collection.getService( "XPathQueryService", "1.0");
      queryService.setProperty( "pretty", "true");
      // queryService.setProperty( "encoding", "ISO-8859-1");
      queryService.setProperty( "encoding", "UTF-8");
                
      ResourceSet result = queryService.query( query);

      ResourceIterator iterator = result.getIterator();
      nodeContentList = new ArrayList();
      elementNameStack = new Stack();
      while (iterator.hasMoreResources()) {
        Resource resource = iterator.nextResource();
        org.w3c.dom.Node domNode = ((LocalXMLResource) resource).getContentAsDOM();
        // create ordered triplet list of element/attribute type, name & value
        parseDomNode( domNode);
//         System.err.println( (String) resource.getContent());
      }
      while (elementNameStack.empty() != true) {
        nodeContentList.add(  new ParsedDomNode
                                 ( new Short ( END_ELEMENT_NODE),
                                   (String) elementNameStack.pop(), ""));
      }
    } catch (XMLDBException e1) {
      System.err.println( "XML:DB Exception in queryCollection 1: " +
                         e1.errorCode + " " + e1.getMessage());
      System.err.println( "query '" + query + "'");
      if (e1.errorCode != 1) {  // skip parse errors
        e1.printStackTrace();
        System.exit( 1);
      }
    } finally {
      try {
        if (collection != null) { collection.close(); }
      } catch (XMLDBException e2) {
        System.err.println( "XML:DB Exception in queryCollection 2: " +
                            e2.errorCode + " " + e2.getMessage());
        e2.printStackTrace();
      }
    }
    return nodeContentList;
  } // end queryCollection


  private static void parseDomNode( org.w3c.dom.Node domNode) {
    String nodeName = domNode.getNodeName();
    String nodeValue = domNode.getNodeValue();
    short nodeType = domNode.getNodeType();
//     System.err.println( "parseDomNode type " + nodeType + "nodeName " + nodeName +
//                         " nodeValue '" + nodeValue + "'");
//      System.err.println( "elementNameStack: " + elementNameStack);
    if (nodeValue == null) {
      nodeValue = "";
    }
    // discard TEXT_NODE:  => ' 
    //              '
    if (nodeValue.indexOf( "\n") == -1) {
      if (nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
        if (elementNameStack.size() != 0) {
          String parentNodeName = domNode.getParentNode().getNodeName();
          while ((elementNameStack.empty() != true) &&
                 (! ((String) elementNameStack.peek()).equals( parentNodeName))) {
            nodeContentList.add( new ParsedDomNode
                                 ( new Short ( END_ELEMENT_NODE),
                                   (String) elementNameStack.pop(), ""));
          }
        }
        elementNameStack.push( nodeName);
      }
      nodeContentList.add( new ParsedDomNode( new Short( nodeType),
                                              nodeName, nodeValue));
    }
    org.w3c.dom.NodeList childNodes = domNode.getChildNodes();
    for (int iNode = 0, nNode = childNodes.getLength(); iNode < nNode; iNode++) {
      parseDomNode( (org.w3c.dom.Node) childNodes.item( iNode));
    }
  } // end parseDomNode

  /**
   * <code>printDomNodeTriplets</code>
   *
   * @param nodeContentList - <code>List</code> - 
   */
  public static void printDomNodeTriplets( List nodeContentList) {
    for (int i = 0, n = nodeContentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) nodeContentList.get( i);
      String typeStr = getNodeTypeString( domNode.getNodeType().shortValue());
      String name = domNode.getNodeName();
      String value = domNode.getNodeValue();
//     System.err.println( "printDomNodeTriplets type " + typeStr + "name " + name +
//                         " value '" + value + "'");
      if (typeStr.equals( getNodeTypeString( org.w3c.dom.Node.ELEMENT_NODE))) {
        System.err.println( "");
      }
      if (! value.equals( "")) {
        System.err.println( typeStr + name + " => " + value);
      } else {
        System.err.println( typeStr + name);
      }
    }
  } // end printDomNodeTriplets


  static class ParsedDomNode {

    private Short nodeType;
    private String nodeName;
    private String nodeValue;

    ParsedDomNode( Short nodeType, String nodeName, String nodeValue) {
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


  private static final String getNodeTypeString( short nodeType) {
    switch (nodeType) {
    case org.w3c.dom.Node.ATTRIBUTE_NODE:
      return "ATTRIBUTE_NODE: "; 
    case org.w3c.dom.Node.CDATA_SECTION_NODE:
      return "CDATA_SECTION_NODE: "; 
    case org.w3c.dom.Node.COMMENT_NODE:
      return "COMMENT_NODE: "; 
    case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE:
      return "DOCUMENT_FRAGMENT_NODE: "; 
    case org.w3c.dom.Node.DOCUMENT_NODE:
      return "DOCUMENT_NODE: "; 
    case org.w3c.dom.Node.DOCUMENT_TYPE_NODE:
      return "DOCUMENT_TYPE_NODE: "; 
    case org.w3c.dom.Node.ELEMENT_NODE:
      return "ELEMENT_NODE: "; 
    case org.w3c.dom.Node.ENTITY_NODE:
      return "ENTITY_NODE: "; 
    case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
      return "ENTITY_REFERENCE_NODE: "; 
    case org.w3c.dom.Node.NOTATION_NODE:
      return "NOTATION_NODE: "; 
    case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
      return "PROCESSING_INSTRUCTION_NODE: "; 
    case org.w3c.dom.Node.TEXT_NODE:
      return "TEXT_NODE: "; 
    case END_ELEMENT_NODE:
      return "END_ELEMENT_NODE: "; 
    default:
      return "UNKNOWN: "; 
    }
  } // end getNodeTypeString


  /**
   * <code>getPartialPlanKeys</code>
   *
   * @param collectionName - <code>String</code> - 
   * @return partialPlanKeys - <code>List of String</code> - 
   */
  public static List getPartialPlanKeys( String collectionName) {
    StringBuffer partialPlanQuery = new StringBuffer( "/");
    partialPlanQuery.append( PARTIAL_PLAN_ELEMENT).append( "/@");
    List partialPlanKeys = 
      queryAttributeValueOfElements( partialPlanQuery + PARTIAL_PLAN_KEY_ATTRIBUTE,
                                     PARTIAL_PLAN_KEY_ATTRIBUTE, collectionName);
    return partialPlanKeys;
  } // end getPartialPlanKeys


  /**
   * <code>queryAttributeValue</code> - return attributeName value for
   *                        element, selected by [@<att>="<value>"]
   *                        /PartialPlan/.../[@<att>="<value>"]@<attributeName>
   *
   * @param query - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return attributeValues - <code>List of String</code> - 
   */
  public static String queryAttributeValue( String query, String collectionName) {
    List contentList = queryCollection( collectionName, query);
    String attributeValue = "";
    for (int i = 0, n = contentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) contentList.get( i);
      short nodeType = domNode.getNodeType().shortValue();
      String nodeName = domNode.getNodeName();
      String nodeValue = domNode.getNodeValue();
      // System.err.println( " type " + nodeType + " name " + nodeName + " value " + nodeValue);
      if (nodeType == org.w3c.dom.Node.ATTRIBUTE_NODE) {
        return nodeValue;
      }
    }
    return attributeValue;
  } // end queryAttributeValueOfElements


  /**
   * <code>queryAttributeValueOfElements</code>
   *
   * @param query - <code>String</code> - 
   * @param attributeName - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>List</code> - 
   */
  public static List queryAttributeValueOfElements( String query, String attributeName,
                                                    String collectionName) {
    List contentList = queryCollection( collectionName, query);
    List attributeValues = new ArrayList();
    for (int i = 0, n = contentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) contentList.get( i);
      short nodeType = domNode.getNodeType().shortValue();
      String nodeName = domNode.getNodeName();
      String nodeValue = domNode.getNodeValue();
      // System.err.println( " type " + nodeType + " name " + nodeName + " value " + nodeValue);
      if ((nodeType == org.w3c.dom.Node.ATTRIBUTE_NODE) &&
          nodeName.equals( attributeName)) {
        attributeValues.add( nodeValue);
      }
    }
    return attributeValues;
  } // end queryAttributeValueOfElements


  /**
   * <code>getPartialPlanModelByKey</code>
   *
   * @param partialPlanKey - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String getPartialPlanModelByKey( String partialPlanKey,
                                                 String collectionName) {
    StringBuffer partialPlanQuery = new StringBuffer( MODEL_KEY_QUERY_PRE);
    partialPlanQuery.append( partialPlanKey).append( MODEL_KEY_QUERY_POST);
    return queryAttributeValue( partialPlanQuery.toString(), collectionName);
  } // end getPartialPlanModel


  /**
   * <code>getPartialPlanObjectsByKey</code>
   *
   * @param partialPlanKey - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>List of String</code> - name, key, name, key, ...
   */
  public static List getPartialPlanObjectsByKey( String partialPlanKey,
                                                 String collectionName) {
    List objectList = new ArrayList();
    StringBuffer objectQuery = new StringBuffer( "/");
    objectQuery.append( PARTIAL_PLAN_ELEMENT).append( "/");
    objectQuery.append( OBJECT_ELEMENT).append( "/@");
    List objectNameList =
      queryAttributeValueOfElements( objectQuery + OBJECT_NAME_ATTRIBUTE,
                                     OBJECT_NAME_ATTRIBUTE, collectionName);
    List objectKeyList =
      queryAttributeValueOfElements( objectQuery + OBJECT_KEY_ATTRIBUTE,
                                     OBJECT_KEY_ATTRIBUTE, collectionName);
    for (int i = 0, n = objectNameList.size(); i < n; i++) {
      objectList.add( (String) objectNameList.get( i));
      objectList.add( (String) objectKeyList.get( i));
    }
    return objectList;
  } // end getPartialPlanObjectsByKey


  private static List getTimelinesSlotsTokens( String collectionName) {
    List nodeContentList = queryCollection( collectionName, PARTIAL_PLAN_OBJECT_QUERY);

//     System.err.println( "\nDOM tree:");
//     printDomNodeTriplets( nodeContentList);

    return nodeContentList;
  } // end getTimelinesSlotsTokens


  /**
   * <code>createTimelineSlotTokenNodesStructure</code>
   *              query => /PartialPlan/Object
   *
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   * @param collectionName - <code>String</code> - 
   */
  public static void createTimelineSlotTokenNodesStructure( PwPartialPlanImpl partialPlan,
                                                            String collectionName) {
    List nodeContentList = getTimelinesSlotsTokens( collectionName);
    String timelineName = "", timelineKey = "";
    int objectIndex = -1; String currentNodeName = ""; PwObjectImpl object = null;
    PwTimelineImpl timeline = null; PwSlotImpl slot = null; List tokenAttributeList = null;
    List objectNodeList = partialPlan.getObjectList();
    for (int i = 0, n = nodeContentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) nodeContentList.get( i);
      short nodeType = domNode.getNodeType().shortValue();
      String nodeName = domNode.getNodeName();
      String nodeValue = domNode.getNodeValue();
      // System.err.println( "type " + nodeType + " name " + nodeName + " value " + nodeValue);
      if (nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
        if (nodeName.equals( OBJECT_ELEMENT)) {
          objectIndex += 1;
          object = (PwObjectImpl) objectNodeList.get( objectIndex);
          currentNodeName = OBJECT_ELEMENT;
        } else if (nodeName.equals( TIMELINE_ELEMENT)) {
          timelineName = ""; timelineKey = "";
          currentNodeName = TIMELINE_ELEMENT;
        } else if (nodeName.equals( SLOT_ELEMENT)) {
          currentNodeName = SLOT_ELEMENT; 
        } else if (nodeName.equals( TOKEN_ELEMENT)) {
          currentNodeName = TOKEN_ELEMENT; tokenAttributeList = new ArrayList();
        } else {
          currentNodeName = "";
        }
      } else if (nodeType == org.w3c.dom.Node.ATTRIBUTE_NODE) {
        if (currentNodeName.equals( TIMELINE_ELEMENT) &&
            nodeName.equals( TIMELINE_KEY_ATTRIBUTE)) {
          timelineKey = nodeValue;
        } else if (currentNodeName.equals( TIMELINE_ELEMENT) &&
                   nodeName.equals( TIMELINE_NAME_ATTRIBUTE)) {
          timelineName = nodeValue;
        } else if (currentNodeName.equals( SLOT_ELEMENT) &&
                   nodeName.equals( SLOT_KEY_ATTRIBUTE)) {
          slot = timeline.addSlot( nodeValue);
        } else if (currentNodeName.equals( TOKEN_ELEMENT)) {
          tokenAttributeList.add( nodeValue);
          if (tokenAttributeList.size() == NUM_TOKEN_ATTRIBUTES) {
            slot.addToken( tokenAttributeList, partialPlan, collectionName);
          }
        }
      }
      if (currentNodeName.equals(TIMELINE_ELEMENT) &&
          (! timelineName.equals( "")) && (! timelineKey.equals( ""))) {
        timeline = object.addTimeline( timelineName, timelineKey);
      }
    }
  } // end createTimelineSlotTokenNodesStructure


  /**
   * <code>queryVariable</code> -- /PartialPlan/Variable[@key="K55"]
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwVariableImpl</code> - 
   */
  public static PwVariableImpl queryVariable( String key, String collectionName) {
    String varType = "", constraintIds = "", paramId = "";
    String enumeration = "", intDomainType = "", lowerBound = "";
    String upperBound = "", currentNodeName = "";
    PwVariableImpl variable = null; PwDomainImpl domain = null;
    StringBuffer query = new StringBuffer( VARIABLE_KEY_QUERY);
    query.append( key).append( END_SELECT_VALUE);
    List contentList = queryCollection( collectionName, query.toString());
    for (int i = 0, n = contentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) contentList.get( i);
      short nodeType = domNode.getNodeType().shortValue();
      String nodeName = domNode.getNodeName();
      String nodeValue = domNode.getNodeValue();
      // System.err.println( " type " + nodeType + " name " + nodeName + " value " + nodeValue);
      if (nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
        if (nodeName.equals( VARIABLE_ELEMENT)) {
          currentNodeName = VARIABLE_ELEMENT;
        } else if (nodeName.equals( ENUMERATED_DOMAIN_ELEMENT)) {
          currentNodeName = ENUMERATED_DOMAIN_ELEMENT;
        } else if (nodeName.equals( INTERVAL_DOMAIN_ELEMENT)) {
          currentNodeName = INTERVAL_DOMAIN_ELEMENT;
        } else { currentNodeName = ""; }
      } else if (nodeType == org.w3c.dom.Node.ATTRIBUTE_NODE) {
        if (currentNodeName.equals( VARIABLE_ELEMENT)) {
          if (nodeName.equals( VARIABLE_TYPE_ATTRIBUTE)) {
            varType = nodeValue;
          } else if (nodeName.equals( VARIABLE_CONSTRAINT_IDS_ATTRIBUTE)) {
            constraintIds = nodeValue;
          } else if (nodeName.equals( VARIABLE_PARAM_ID_ATTRIBUTE)) {
            paramId = nodeValue;
          }
        } else if (currentNodeName.equals( INTERVAL_DOMAIN_ELEMENT)) {
          if (nodeName.equals( INTERVAL_DOMAIN_TYPE_ATTRIBUTE)) {
            intDomainType = nodeValue;
          } else if (nodeName.equals( INTERVAL_DOMAIN_LOWER_BOUND_ATTRIBUTE)) {
            lowerBound = nodeValue;
          } else if (nodeName.equals( INTERVAL_DOMAIN_UPPER_BOUND_ATTRIBUTE)) {
            upperBound = nodeValue;
          }
        }
      } else if ((nodeType == org.w3c.dom.Node.TEXT_NODE) &&
                 currentNodeName.equals( ENUMERATED_DOMAIN_ELEMENT)) {
        enumeration = nodeValue;
      }
    } // end for
    if (enumeration.equals( "")) {
      domain = new PwIntervalDomainImpl( intDomainType, lowerBound, upperBound);
    } else {
      domain = new PwEnumeratedDomainImpl( enumeration);
    }
    variable = new PwVariableImpl( key, varType, constraintIds, paramId, domain);
    return variable;
  } // end queryVariable


  /**
   * <code>queryPredicate</code> - /PartialPlan/Predicate[@key="K32"]
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return predicate - <code>PwPredicate</code> - 
   */
  public static PwPredicateImpl queryPredicate( String key, String collectionName) {
    PwPredicateImpl predicate = null;
    String parameterKey = "", currentNodeName = "";
    StringBuffer query = new StringBuffer( PREDICATE_KEY_QUERY);
    query.append( key).append( END_SELECT_VALUE);
    List contentList = queryCollection( collectionName, query.toString());
    for (int i = 0, n = contentList.size(); i < n; i++) {
      ParsedDomNode domNode = (ParsedDomNode) contentList.get( i);
      short nodeType = domNode.getNodeType().shortValue();
      String nodeName = domNode.getNodeName();
      String nodeValue = domNode.getNodeValue();
      // System.err.println( " type " + nodeType + " name " + nodeName + " value " + nodeValue);
      if (nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
        if (nodeName.equals( PREDICATE_ELEMENT)) {
          currentNodeName = PREDICATE_ELEMENT;
        } else if (nodeName.equals( PARAMETER_ELEMENT)) {
          currentNodeName = PARAMETER_ELEMENT;
        } else { currentNodeName = ""; }
      } else if (nodeType == org.w3c.dom.Node.ATTRIBUTE_NODE) {
        if (currentNodeName.equals( PREDICATE_ELEMENT)) {
          if (nodeName.equals( PREDICATE_NAME_ATTRIBUTE)) {
            predicate = new PwPredicateImpl( nodeValue, key);
          }
        } else if (currentNodeName.equals( PARAMETER_ELEMENT)) {
          if (nodeName.equals( PARAMETER_KEY_ATTRIBUTE)) {
            parameterKey = nodeValue;
          } else if (nodeName.equals( PARAMETER_NAME_ATTRIBUTE)) {
            predicate.addParameter( nodeValue, parameterKey);
          }
        }
      }
    }
    return predicate;
  } // end queryPredicate

} // end class XmlDBeXist








