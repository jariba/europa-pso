//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.11 2003-06-25 17:04:05 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.ParsedDomNode;
//import org.w3c.dom.Node;

/*
 * <code>ContentSpec</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The content specification class.  Interfaces directely with the database to determine which keys
 * are/should be in the current specification, and provides a method for exposing that information
 * to other classes (VizView through ViewSet).  It uses <code>BitSet</code>s to determine validity
 * and perform logic functions.
 */

public class ContentSpec {
  //private static final String EQ = "=";
  private static final String EQ = "&=";
  private static final String NE = "!=";
  private static final String OR = " | ";
  private static final String TIMELINE = "/Timeline";
  private static final String SLOT = "/Slot";
  private static final String TOKEN = "/Token";
  private static final String VARIABLE = "/Variable";
  private static final String CONSTRAINT ="/Constraint";
  private static final String PREDICATE = "/Predicate";
  private static final String PARAMETER = "/Parameter";
  private static final String KEY = "@key";
  private static final String PREDID = "@predicateId";
  private static final String SVID = "@startVarId";
  private static final String EVID = "@endVarId";
  private static final String DVID = "@durationVarId";
  private static final String OVID = "@objectVarId"; //RIP17CE
  private static final String RVID = "@rejectVarId";
  private static final String TOKRELIDS = "@tokenRelationIds";
  private static final String PARAMVIDS = "@paramVarIds";
  private static final String CONSTRIDS = "@constraintIds";
  private static final String PARAMID = "@paramid";
  private static final String VARIDS = "@variableIds";
  private static final String VTYPE = "@type";
  
  private BitSet currentSpec;
  private String collectionName;
  private RedrawNotifier redrawNotifier;

  /**
   * Creates the ContentSpec object, then makes a query to determine the size of the
   * <code>BitSet</code> that represents the current specification, and sets all of the bits
   * to <code>true</code>: everything is in the spec initially.
   * @param collectionName the name of the collection in eXist which contains the plan step with
   *                       which this ContentSpec is associated
   * @param redrawNotifier is used to notify the views governed by the spec that the spec has
   *                       changed, and they need to redraw themselves.
   */

  public ContentSpec(String collectionName, RedrawNotifier redrawNotifier) {
    this.collectionName = collectionName;
    this.redrawNotifier = redrawNotifier;

    int highestIndex = 0;
    //there may be a better way to do this
    System.err.println("contentSpec: querying " + collectionName);
    //replace this with the count() function!
//     List nodeList = XmlDBeXist.queryCollection(collectionName, "/PartialPlan/*/@key");
//     System.err.println("contentSpec: got " + nodeList.size() + " keys.");
//     ListIterator nodeListIterator = nodeList.listIterator();
//     while(nodeListIterator.hasNext()) {
//       ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
//       if(node.getNodeName().equals("key")) {
//         int index = keyToIndex(node.getNodeValue());
//         if(index > highestIndex) {
//           highestIndex = index;
//         }
//       }
//     }
    highestIndex = 150;
    System.err.println("Content spec highest index: " + highestIndex);
    currentSpec = new BitSet(highestIndex + 1);
    currentSpec.set(0, highestIndex, true);
  }
  /**
   * Sets all of the bits to true, then informs the views governed by the spec that they need to
   * redraw.
   */
  public void resetSpec() {
    currentSpec.set(0, currentSpec.size()-1, true);
    redrawNotifier.notifyRedraw();
  }
  /**
   * Given a key, returns true or false depending on whether or not the key is in the current
   * spec.
   * @param key the key being tested.
   */
  public boolean isInContentSpec(String key) {
    int index;
    try{index = keyToIndex(key);}
    catch(NumberFormatException nfe){return false;} //maybe this should do something else
    if(index > currentSpec.length()) {
      currentSpec.clear(index);
    }
    if(index < 0)
      return false;
    return currentSpec.get(index);
  }
  public void printSpec() {
    System.err.println("Allowable keys: ");
    for(int i = 0; i < currentSpec.size(); i++) {
      if(currentSpec.get(i)) {
        System.err.println("K" + i);
      }
    }
  }
  /**
   * Given a key, returns the integer part.  Used to index into the BitSet.
   * @param key the key being converted.
   */
  private int keyToIndex(String key) throws NumberFormatException {
    return Integer.parseInt(key.substring(1)); //i hope the key format never changes
  }
  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid keys through a series of database queries, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   * @param timeline the result of getValues() in TimelineGroupBox.
   * @param predicate the result of getValues() in PredicateGroupBox.
   * @param constraint the result of getValues() in ConstraintGroupBox.
   * @param variableType the result of getValues() in VariableTypeGroupBox.
   * @param timeInterval the result of getValues() in TimeIntervalGroupBox.
   */
  //REALLY INEFFICIENT INITIAL RUN.  some of this should probably be refactored
  public void applySpec(List timeline, List predicate, List constraint, List variableType, 
                        List timeInterval) throws NumberFormatException {
    //currentSpec.clear();
    boolean cleared = false;
    BitSet partialSpec = new BitSet(currentSpec.size());


    if(timeline != null) {
      System.err.println("Timeline spec " + timeline.size());
      if(!cleared) {
        currentSpec.clear();
        cleared = true;
      }
      for(int i = 1; i < timeline.size(); i += 2) {
        System.err.println("timeline spec for key " + (String)timeline.get(i));
        //int topIndex = keyToIndex((String)timeline.get(i));
        //if(topIndex > 0) {
        //  partialSpec.set(topIndex, false);
        //}
        long startTime = (new Date()).getTime();
        //build query strings
        StringBuffer timelineQuery = new StringBuffer();
        //quick path to timeline.  doing it with shortcuts may be faster.
        StringBuffer pathToTimeline = new StringBuffer("/PartialPlan/");
        // //Timeline[@key="key"]
        pathToTimeline.append(TIMELINE).append("[").append(KEY).append(EQ).append("\"");
        pathToTimeline.append((String)timeline.get(i)).append("\"]");
        //quick path to token.
        //StringBuffer pathToToken = new StringBuffer(pathToTimeline.toString());
        // //Timeline[@key="key"]//Token/
        //pathToToken.append("/").append(TOKEN).append("/");
        //timelineQuery.append(pathToTimeline).append("/@key").append(OR);
        // //Timeline[@key="Key"]/Slot/@key | 
        //timelineQuery.append(pathToTimeline).append(SLOT).append("/").append(KEY).append(OR);
        // //Timeline[@key="Key"]//Token/@*
        //timelineQuery.append(pathToToken).append("@*");
        //timelineQuery.append(OR).append("/PartialPlan/*/Timeline/parent::Object/@key");
        timelineQuery.append(pathToTimeline).append("/@key").append(OR);
        timelineQuery.append(pathToTimeline).append("//@*");
        System.err.println("Timeline query: " + timelineQuery.toString());
        //query
        long queryTime1 = (new Date()).getTime();
        List nodeList = XmlDBeXist.queryCollection(collectionName, timelineQuery.toString());
        queryTime1 = (new Date()).getTime() - queryTime1;
        System.out.println("Got " + nodeList.size() + " nodes.");
        if(nodeList.size() == 0){
          return;
        }
        ListIterator nodeListIterator = nodeList.listIterator();
        ArrayList varKeys = new ArrayList();
        //iterate through resultant nodes
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          String nodeName = node.getNodeName();
          //if the node contains a composite value, break it up
          if(nodeName.equals("tokenRelationIds") || nodeName.equals("paramVarIds")) {
            StringTokenizer strTok = new StringTokenizer(node.getNodeValue(), " ", false);
            while(strTok.hasMoreTokens()) {
              String key = strTok.nextToken();
              int index = keyToIndex(key);
              if(index > 0) {
                partialSpec.set(index);
              }
              //if it's a parameter value, save it for later
              if(nodeName.equals("paramVarIds")) {
                varKeys.add(key);
              }
            }
            continue;
          }
          //set key in partialSpec to true
          else if(nodeName.equals("key") || nodeName.indexOf("Id") != -1) {
            int index = keyToIndex(node.getNodeValue());
            if(index > 0) {
              partialSpec.set(index);
            }
            //save variable ids for getting constraints and parameters
            if(nodeName.indexOf("Var") != -1) {
              varKeys.add(node.getNodeValue());
            }
          }
        }
        //get the constraints of the variables
        StringBuffer constraintQuery = new StringBuffer("/");
        constraintQuery.append(CONSTRAINT).append("[");
        //for(int j = 0; j < varKeys.size()-2; j++) {
          //constraintQuery.append("contains(").append(VARIDS).append(", \"");
          //constraintQuery.append((String)varKeys.get(j)).append("\") or ");
          //}
        //constraintQuery.append("contains(").append(VARIDS).append(", \"");
        //constraintQuery.append((String)varKeys.get(varKeys.size()-1)).append("\")]/").append(KEY);
        constraintQuery.append(VARIDS).append("|=\"");

        StringBuffer varQuery = new StringBuffer("/");
        varQuery.append(VARIABLE).append("[@key|=\"");
        for(int j = 0; j < varKeys.size()-2; j++) {
          constraintQuery.append((String)varKeys.get(j)).append(" ");
          varQuery.append((String)varKeys.get(j)).append(" ");
        }
        constraintQuery.append((String)varKeys.get(varKeys.size()-1)).append("\"]/").append(KEY);
        varQuery.append((String)varKeys.get(varKeys.size()-1)).append("\"]/").append(PARAMID);
        System.err.println("constraint query: " + constraintQuery.toString());
        long queryTime2 = (new Date()).getTime();
        nodeList = XmlDBeXist.queryCollection(collectionName, constraintQuery.toString());
        queryTime2 = (new Date()).getTime() - queryTime2;
        nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("key")) { //this check may be superfluous
            int index = keyToIndex(node.getNodeValue());
            if(index > 0) {
              partialSpec.set(index);
            }
          }
        }
        //get the parameter ids
        /*        StringBuffer varQuery = new StringBuffer("/");
                  varQuery.append(VARIABLE).append("[@key|=\"");
        //for(int j = 0; j < varKeys.size()-2; j++) {
          //varQuery.append("@key=\"").append((String)varKeys.get(j)).append("\" or ");
        //varQuery.append("@key&=\"").append((String)varKeys.get(j)).append("\" or ");
        //}
        //varQuery.append("@key=\"").append((String)varKeys.get(varKeys.size()-1)).append("\"]/");
        //varQuery.append("@key&=\"").append((String)varKeys.get(varKeys.size()-1)).append("\"]/");
        varQuery.append(PARAMID);*/
        System.err.println("Variable query: " + varQuery.toString());
        long queryTime3 = (new Date()).getTime();
        nodeList = XmlDBeXist.queryCollection(collectionName, varQuery.toString());
        queryTime3 = (new Date()).getTime() - queryTime3;
        nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("paramId")) {
            int index = keyToIndex(node.getNodeValue());
            if(index > 0) {
              partialSpec.set(index);
            }
          }
        }
        System.err.println("First run query took " + ((new Date()).getTime() - startTime) +
                           "milliseconds");
        queryTime1 += queryTime2 + queryTime3;
        System.err.println("Spent " + queryTime1 + " milliseconds in queries.");
        String connective = (String) timeline.get(i-1);
        if(connective.indexOf("not") != -1) {
          partialSpec.flip(0, partialSpec.size()-1);
        }
        if(connective.indexOf("and") != -1) {
          currentSpec.and(partialSpec);
        }
        else {
          currentSpec.or(partialSpec);
        }
        partialSpec.clear();
      }
    }


    if(predicate != null) {
      if(!cleared) {
        currentSpec.clear();
        cleared = true;
      }
      for(int i = 1; i < predicate.size(); i += 2) {
        StringBuffer predicateQuery = new StringBuffer();
        StringBuffer pathToToken = new StringBuffer("/");
        // //Token[@predicateId="Key"]
        pathToToken.append(TOKEN).append("[").append(PREDID).append(EQ).append("\"");
        pathToToken.append((String) predicate.get(i)).append("\"]/");
        // //Predicate[@key="key"]/Parameter/@key |
        predicateQuery.append("/").append(PREDICATE).append("[").append(KEY).append(EQ);
        predicateQuery.append("\"").append((String)predicate.get(i)).append("\"]");
        predicateQuery.append(PARAMETER).append("/").append(KEY).append(OR);
        // //Token[@predicateId="key"]/@*
        predicateQuery.append(pathToToken).append("@*");
        List nodeList = XmlDBeXist.queryCollection(collectionName, predicateQuery.toString());
        ListIterator nodeListIterator = nodeList.listIterator();
        ArrayList varKeys = new ArrayList();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          String nodeName = node.getNodeName();
          if(nodeName.equals("tokenRelationIds") || nodeName.equals("paramVarIds")) {
            StringTokenizer strTok = new StringTokenizer(node.getNodeValue(), " ", false);
            while(strTok.hasMoreTokens()) {
              String key = strTok.nextToken();
              partialSpec.set(keyToIndex(key));
              if(nodeName.equals("paramVarIds")) {
                varKeys.add(key);
              }
            }
            continue;
          }
          else if(nodeName.equals("key") || nodeName.indexOf("Id") != -1) {
            partialSpec.set(keyToIndex(node.getNodeValue()));
            if(nodeName.indexOf("Var") != -1) {
              varKeys.add(node.getNodeValue());
            }
          }
        }
        //variable constraint and param query
        //get the constraints of the variables
        StringBuffer constraintQuery = new StringBuffer("/");
        constraintQuery.append(CONSTRAINT).append("[");
        for(int j = 0; j < varKeys.size()-2; j++) {
          constraintQuery.append("contains(").append(VARIDS).append(", \"");
          constraintQuery.append((String)varKeys.get(j)).append("\") or ");
        }
        constraintQuery.append("contains(").append(VARIDS).append(", \"");
        constraintQuery.append((String)varKeys.get(varKeys.size()-1)).append("\")]/").append(KEY);
        nodeList = XmlDBeXist.queryCollection(collectionName, constraintQuery.toString());
        nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("key")) { //this check may be superfluous
            partialSpec.set(keyToIndex(node.getNodeValue()));
          }
        }
        //get the parameter ids
        StringBuffer varQuery = new StringBuffer("/");
        varQuery.append(VARIABLE).append("[");
        for(int j = 0; j < varKeys.size()-2; j++) {
          varQuery.append("@key=\"").append((String)varKeys.get(j)).append("\" or ");
        }
        varQuery.append("@key=\"").append((String)varKeys.get(varKeys.size()-1)).append("\"]/");
        varQuery.append(PARAMID);
        nodeList = XmlDBeXist.queryCollection(collectionName, constraintQuery.toString());
        nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("paramId")) {
            partialSpec.set(keyToIndex(node.getNodeValue()));
          }
        }
        String connective = (String) predicate.get(i-1);
        if(connective.indexOf("not") != -1) {
          partialSpec.flip(0, partialSpec.size()-1);
        }
        if(connective.indexOf("and") != -1) {
          currentSpec.and(partialSpec);
        }
        else {
          currentSpec.or(partialSpec);
        }
        partialSpec.clear();
      }
    }


    if(constraint != null) {
      if(!cleared) {
        currentSpec.clear();
        cleared = true;
      }
      for(int i = 1; i < constraint.size(); i += 2) {
        StringBuffer constraintQuery = new StringBuffer("/");
        constraintQuery.append(CONSTRAINT).append("[").append(KEY).append(EQ).append("\"");
        constraintQuery.append((String) constraint.get(i)).append("\"]/").append(VARIDS);
        //System.out.println("Constraint query:");
        //System.out.println(constraintQuery.toString());
        //tokenize result
        List nodeList = XmlDBeXist.queryCollection(collectionName, constraintQuery.toString());
        ListIterator nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
            ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
            if(node.getNodeName().equals("variableIds")) {
              StringTokenizer strTok = new StringTokenizer(node.getNodeValue(), " ", false);
              while(strTok.hasMoreTokens()) {
                partialSpec.set(keyToIndex(strTok.nextToken()));
              }
            }
        }
        String connective = (String) constraint.get(i-1);
        if(connective.indexOf("not") != -1) {
          partialSpec.flip(0, partialSpec.size()-1);
        }
        if(connective.indexOf("and") != -1) {
          currentSpec.and(partialSpec);
        }
        else {
          currentSpec.or(partialSpec);
        }
        partialSpec.clear();
      }
    }


    if(variableType != null) {
      if(!cleared) {
        currentSpec.clear();
        cleared = true;
      }
      for(int i = 1; i < variableType.size(); i += 2) {
        StringBuffer variableQuery = new StringBuffer("/");
        variableQuery.append(VARIABLE).append("[").append(VTYPE).append(EQ).append("\"");
        variableQuery.append((String) variableType.get(i)).append("\"]/").append(KEY);
        //System.out.println("Variable query:");
        //System.out.println(variableQuery.toString());
        List nodeList = XmlDBeXist.queryCollection(collectionName, variableQuery.toString());
        ListIterator nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("key")) {
            partialSpec.set(keyToIndex(node.getNodeValue()));
          }
        }
        String connective = (String) variableType.get(i-1);
        if(connective.indexOf("not") != -1) {
          partialSpec.flip(0, partialSpec.size()-1);
        }
        if(connective.indexOf("and") != -1) {
          currentSpec.and(partialSpec);
        }
        else {
          currentSpec.or(partialSpec);
        }
        partialSpec.clear();
      }
    }

    if(timeInterval != null) {
      if(!cleared) {
        currentSpec.clear();
        cleared = true;
      }
      for(int i = 1; i < timeInterval.size(); i += 3) {
        StringBuffer intervalQuery = (new StringBuffer("//Variable[(@type=\"START_VAR\" and (IntervalDomain[@lowerBound >= ")).append(Integer.parseInt((String)timeInterval.get(i))).append("] or EnumeratedDomain[contains(text(), \"").append(timeInterval.get(i)).append("\")])) or (@type=\"END_VAR\" and (IntervalDomain[@upperBound <= ").append(Integer.parseInt((String)timeInterval.get(i+1))).append("] or EnumeratedDomain[contains(text(), \"").append(timeInterval.get(i+1)).append("\")]))]/@key");
        List nodeList = XmlDBeXist.queryCollection(collectionName, intervalQuery.toString());
        ListIterator nodeListIterator = nodeList.listIterator();
        StringBuffer tokenQuery = new StringBuffer("//Token[@startVarId=\"\" ");
        StringBuffer constraintQuery = new StringBuffer("//Constraint[contains(@variableIds, \"\") ");
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          if(node.getNodeName().equals("key")) {
            partialSpec.set(keyToIndex(node.getNodeValue()));
            tokenQuery.append("or @startVarId=\"").append(node.getNodeValue()).append("\" or @endVarId=\"").append(node.getNodeValue()).append("\" ");
            constraintQuery.append("or contains(@variableIds, \"").append(node.getNodeValue()).append("\") ");
          }
        }
        constraintQuery.append("]/@key");
        tokenQuery.append("]");
        StringBuffer everythingElseQuery = new StringBuffer();
        everythingElseQuery.append(constraintQuery).append(" | ").append(tokenQuery);
        everythingElseQuery.append("/@key").append(" | ").append(tokenQuery).append("/@predicateId").append(" | ").append(tokenQuery).append("/@paramVarIds").append(" | ").append(tokenQuery).append("/@tokenRelationIds").append(" | ").append(tokenQuery).append("/@durationVarId").append(" | ").append(tokenQuery).append("/@rejectVarId").append(" | ").append(tokenQuery).append("/@slotId").append(" | ").append("//Timeline[Slot/@key=").append(tokenQuery).append("/@slotId]/@key").append(" | ").append("/Object[Timeline/@key=//Timeline[Slot/@key=").append(tokenQuery).append("/@slotId]/@key]/@key").append(" | ").append("//Predicate[@key=").append(tokenQuery).append("/@predicateId]/Parameter/@key");
        nodeList = XmlDBeXist.queryCollection(collectionName, everythingElseQuery.toString());
        nodeListIterator = nodeList.listIterator();
        while(nodeListIterator.hasNext()) {
          ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
          String nodeName = node.getNodeName();
          if(nodeName.indexOf("Ids") != -1) {
            StringTokenizer strTok = new StringTokenizer(node.getNodeValue(), " ", false);
            while(strTok.hasMoreTokens()) {
              partialSpec.set(keyToIndex(strTok.nextToken()));
            }
          }
          else {
            partialSpec.set(keyToIndex(node.getNodeValue()));
          }
        }
        String connective = (String) variableType.get(i-1);
        if(connective.indexOf("not") != -1) {
          partialSpec.flip(0, partialSpec.size()-1);
        }
        if(connective.indexOf("and") != -1) {
          currentSpec.and(partialSpec);
        }
        else {
          currentSpec.or(partialSpec);
        }
        partialSpec.clear();
      }
    }
    List nodeList = XmlDBeXist.queryCollection(collectionName, "/PartialPlan/@key | /PartialPlan/Object/@key");
    ListIterator nodeListIterator = nodeList.listIterator();
    while(nodeListIterator.hasNext()) {
      ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
      if(node.getNodeName().equals("key")) {
        int index = keyToIndex(node.getNodeValue());
        if(index > 0) {
          currentSpec.set(index);
        }
      }
    }
    redrawNotifier.notifyRedraw();
  }
  public void executeQuery(String query) {
    System.err.println("Test: executing query " + query);
    long t1 = System.currentTimeMillis();
    List nodeList = XmlDBeXist.queryCollection(collectionName, query);
    System.err.println("Test: query took " + (System.currentTimeMillis() - t1) + "ms");
    System.err.println("Test: got " + nodeList.size() + " nodes.");
    ListIterator nodeListIterator = nodeList.listIterator();
    while(nodeListIterator.hasNext()) {
      ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
      System.err.println("NODE name: " + node.getNodeName() + " type: " + node.getNodeType() +
                         " value: " + node.getNodeValue());
    }
  }
}
