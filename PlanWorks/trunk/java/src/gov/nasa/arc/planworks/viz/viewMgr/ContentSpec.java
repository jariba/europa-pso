//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.3 2003-06-11 00:32:10 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.ParsedDomNode;
//import org.w3c.dom.Node;

public class ContentSpec {
  private static final String EQ = "=";
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
  public ContentSpec(String collectionName) {
    this.collectionName = collectionName;

    int highestIndex = 0;
    //there may be a better way to do this
    List nodeList = XmlDBeXist.queryCollection(collectionName, "//@key");
    ListIterator nodeListIterator = nodeList.listIterator();
    while(nodeListIterator.hasNext()) {
      ParsedDomNode node = (ParsedDomNode) nodeListIterator.next();
      if(node.getNodeName().equals("key")) {
        int index = keyToIndex(node.getNodeValue());
        if(index > highestIndex) {
          highestIndex = index;
        }
      }
    }
    System.out.println("Content spec highest index: " + highestIndex);
    currentSpec = new BitSet(highestIndex + 1);
    currentSpec.set(0, highestIndex, true);
  }
  public void resetSpec() {
    currentSpec.set(0, currentSpec.size()-1, true);
  }
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
  private int keyToIndex(String key) throws NumberFormatException {
    return Integer.parseInt(key.substring(1)); //i hope the key format never changes
  }
  //REALLY INEFFICIENT INITIAL RUN.  some of this should probably be refactored
  public void applySpec(List timeline, List predicate, List constraint, List variableType, 
                        List timeInterval) throws NumberFormatException {
    currentSpec.clear();
    BitSet partialSpec = new BitSet(currentSpec.size());


    if(timeline != null) {
      for(int i = 1; i < timeline.size(); i += 2) {
        //build query strings
        StringBuffer timelineQuery = new StringBuffer();
        //quick path to timeline.  doing it with shortcuts may be faster.
        StringBuffer pathToTimeline = new StringBuffer("/");
        // //Timeline[@key="key"]
        pathToTimeline.append(TIMELINE).append("[").append(KEY).append(EQ).append("\"");
        pathToTimeline.append((String)timeline.get(i)).append("\"]");
        //quick path to token.
        StringBuffer pathToToken = new StringBuffer(pathToTimeline.toString());
        // //Timeline[@key="key"]//Token/
        pathToToken.append("/").append(TOKEN).append("/");
        // //Timeline[@key="Key"]/Slot/@key | 
        timelineQuery.append(pathToTimeline).append(SLOT).append("/").append(KEY).append(OR);
        // //Timeline[@key="Key"]//Token/@*
        timelineQuery.append(pathToToken).append("@*");
        //query
        List nodeList = XmlDBeXist.queryCollection(collectionName, timelineQuery.toString());
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
              partialSpec.set(keyToIndex(key));
              //if it's a parameter value, save it for later
              if(nodeName.equals("paramVarIds")) {
                varKeys.add(key);
              }
            }
            continue;
          }
          //set key in partialSpec to true
          else if(nodeName.equals("key") || nodeName.indexOf("Id") != -1) {
            partialSpec.set(keyToIndex(node.getNodeValue()));
            //save variable ids for getting constraints and parameters
            if(nodeName.indexOf("Var") != -1) {
              varKeys.add(node.getNodeValue());
            }
          }
        }
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
  }
}
