//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.5 2003-08-12 22:54:18 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
//import gov.nasa.arc.planworks.db.util.XmlDBeXist;
//import gov.nasa.arc.planworks.db.util.ParsedDomNode;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.viz.viewMgr.RedrawNotifier;
//import org.w3c.dom.Node;

/*
 * <code>ContentSpec</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The content specification class.  Interfaces directely with the database to determine which ids
 * are/should be in the current specification, and provides a method for exposing that information
 * to other classes (VizView through ViewSet).  It uses <code>BitSet</code>s to determine validity
 * and perform logic functions.
 */

public class ContentSpec {
  private ArrayList validTokenIds;
  private Long partialPlanId;
  private PwPartialPlan partialPlan;
  private RedrawNotifier redrawNotifier;

  private static final String AND = "and";
  private static final String AND_PPREDICATEID = "&& (PredicateId";
  private static final String AND_PREDICATEID = "&& PredicateId";
  private static final String AND_PTIMELINEID = "&& (TimelineId";
  private static final String AND_TIMELINEID = "&& TimelineId";
  private static final String EQ = "=";
  private static final String NEG = "!";
  private static final String NOT = "not";
  private static final String OBJECTNAME = "Object.ObjectName";
  private static final String OR = "or";
  private static final String OR_TIMELINEID = "|| TimelineId";
  private static final String OR_PREDICATEID = "|| PredicateId";
  private static final String PREDICATEID = "PredicateId";
  private static final String PREDICATENAME = "PredicateName";
  private static final String PREDICATENAME_QUERY =
    "SELECT PredicateName, PredicateId FROM Predicate WHERE PartialPlanId=";
  private static final String TIMELINEID = "Timeline.TimelineId";
  private static final String TIMELINENAME = "Timeline.TimelineName";
  private static final String TIMELINENAME_QUERY =
    "SELECT Object.ObjectName, Timeline.TimelineName, Timeline.TimelineId FROM Object RIGHT JOIN Timeline ON Timeline.ObjectId=Object.ObjectId && Timeline.PartialPlanId=Object.PartialPlanId WHERE Object.PartialPlanId=";
  private static final String TOKENID = "TokenId";
  private static final String TOKENID_QUERY = "SELECT TokenId FROM Token WHERE PartialPlanId=";
  /**
   * Creates the ContentSpec object, then makes a query to determine the size of the
   * <code>BitSet</code> that represents the current specification, and sets all of the bits
   * to <code>true</code>: everything is in the spec initially.
   * @param collectionName the name of the collection in eXist which contains the plan step with
   *                       which this ContentSpec is associated
   * @param redrawNotifier is used to notify the views governed by the spec that the spec has
   *                       changed, and they need to redraw themselves.
   */

  public ContentSpec(PwPartialPlan partialPlan, RedrawNotifier redrawNotifier) 
    throws SQLException {
    this.partialPlanId = partialPlan.getId();
    this.partialPlan = partialPlan;
    this.redrawNotifier = redrawNotifier;
    this.validTokenIds = new ArrayList();
    queryValidTokens();
  }
  /**
   * Sets all of the bits to true, then informs the views governed by the spec that they need to
   * redraw.
   */
  public void resetSpec() throws SQLException {
    validTokenIds.clear();
    queryValidTokens();
    redrawNotifier.notifyRedraw();
  }
  private void queryValidTokens() throws SQLException {
    ResultSet validTokens = MySQLDB.queryDatabase(TOKENID_QUERY.concat(partialPlanId.toString()));
    while(validTokens.next()) {
      validTokenIds.add(new Integer(validTokens.getInt(TOKENID)));
    }
  }
  public List getValidTokenIds(){return validTokenIds;}

  public void printSpec() {
    System.err.println("Allowable tokens: ");
    ListIterator tokenIdIterator = validTokenIds.listIterator();
    while(tokenIdIterator.hasNext()) {
      System.err.println(((Integer)tokenIdIterator.next()).toString());
    }
  }
  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid ids through a series of database queries, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   * @param timeline the result of getValues() in TimelineGroupBox.
   * @param predicate the result of getValues() in PredicateGroupBox.
   * @param timeInterval the result of getValues() in TimeIntervalGroupBox.
   */
  public void applySpec(List timeline, List predicate, List timeInterval) 
    throws NumberFormatException, SQLException {
    StringBuffer tokenQuery = new StringBuffer(TOKENID_QUERY);
    tokenQuery.append(partialPlanId.toString()).append(" ");
    if(timeline != null) {
      tokenQuery.append(AND_PTIMELINEID);
      if(((String)timeline.get(0)).indexOf(NOT) != -1) {
        tokenQuery.append(NEG);
      }
      tokenQuery.append(EQ);
      tokenQuery.append(((Integer)timeline.get(1)).toString()).append(" ");
      for(int i = 2; i < timeline.size(); i++) {
        String connective = (String) timeline.get(i);
        if(connective.indexOf(AND) != -1) {
          tokenQuery.append(AND_TIMELINEID);
        }
        else {
          tokenQuery.append(OR_TIMELINEID);
        }
        if(connective.indexOf(NOT) != -1) {
          tokenQuery.append(NEG);
        }
        i++;
        tokenQuery.append(EQ).append(((Integer)timeline.get(i)).toString()).append(" ");
      }
      tokenQuery.append(") ");
    }
    if(predicate != null) {
      tokenQuery.append(AND_PPREDICATEID);
      if(((String)predicate.get(0)).indexOf(NOT) != -1) {
        tokenQuery.append(NEG);
      }
      tokenQuery.append(EQ);
      tokenQuery.append(((Integer)predicate.get(1)).toString()).append(" ");
      for(int i = 2; i < predicate.size(); i++) {
        String connective = (String) predicate.get(i);
        if(connective.indexOf(AND) != -1) {
          tokenQuery.append(AND_PREDICATEID);
        }
        else {
          tokenQuery.append(OR_PREDICATEID);
        }
        if(connective.indexOf(NOT) != -1) {
          tokenQuery.append(NEG);
        }
        i++;
        tokenQuery.append(EQ).append(((Integer)predicate.get(i)).toString()).append(" ");
      }
      tokenQuery.append(") ");
    }
    tokenQuery.append(";");
    ResultSet tokenIds = MySQLDB.queryDatabase(tokenQuery.toString());
    validTokenIds.clear();
    while(tokenIds.next()) {
      validTokenIds.add(new Integer(tokenIds.getInt(TOKENID)));
    }
    //this probably takes too long
    if(timeInterval != null) {
      System.err.println(timeInterval);
      ListIterator tokenIdIterator = validTokenIds.listIterator();
      while(tokenIdIterator.hasNext()) {
        Integer tokenId = (Integer) tokenIdIterator.next();
        Integer earliestStart = partialPlan.getToken(tokenId).getEarliestStart();
        Integer latestEnd = partialPlan.getToken(tokenId).getLatestEnd();
        boolean leftIsTrue = false;

        for(int i = 0; i < timeInterval.size(); i += 3) {
          String connective = (String) timeInterval.get(i);
          Integer start = (Integer) timeInterval.get(i+1);
          Integer end = (Integer) timeInterval.get(i+2);
          if(connective.indexOf(AND) > -1) {
            if(!leftIsTrue) {
              break;
            }
            leftIsTrue = evaluateTimeInterval(connective, start, end, earliestStart, latestEnd);
            if(!leftIsTrue) {
              break;
            }
          }
          else if(connective.indexOf(NOT) > -1) {
            leftIsTrue = 
              (evaluateTimeInterval(connective, start, end, earliestStart, latestEnd) || 
               leftIsTrue);
          }
        }
        if(!leftIsTrue) {
          tokenIdIterator.remove();
        }
      }
    }
    redrawNotifier.notifyRedraw();
  }

  private boolean evaluateTimeInterval(String connective, Integer start, Integer end, 
                                       Integer earliestStart, Integer latestEnd) {
    boolean negation = (connective.indexOf(NOT) > -1);
    System.err.println(negation);
    if(earliestStart.compareTo(start) >= 0 && earliestStart.compareTo(end) <= 0) {
      return true ^ negation;
    }
    if(latestEnd.compareTo(start) >= 0 && latestEnd.compareTo(end) <= 0) {
      return true ^ negation;
    }
    if(earliestStart.compareTo(start) <= 0 && latestEnd.compareTo(end) >= 0) {
      return true ^ negation;
    }
    return false ^ negation;
  }

  public Map getPredicateNames() throws SQLException {
    HashMap predicates = new HashMap();
    
    System.err.println("Getting predicate names...");
    ResultSet predicateNames = 
      MySQLDB.queryDatabase(PREDICATENAME_QUERY.concat(partialPlanId.toString()));
    while(predicateNames.next()) {
      predicates.put(predicateNames.getString(PREDICATENAME), 
                       new Integer(predicateNames.getInt(PREDICATEID)));
    }
    return predicates;
  }
  public Map getTimelineNames() throws SQLException {
    HashMap timelines = new HashMap();
    System.err.println("Getting timeline names...");
    ResultSet timelineNames =
      MySQLDB.queryDatabase(TIMELINENAME_QUERY.concat(partialPlanId.toString()));
    String objName = null;
    while(timelineNames.next()) {
      objName = (timelineNames.getString(OBJECTNAME) == null ? objName : 
                 timelineNames.getString(OBJECTNAME));
      timelines.put("".concat(objName).concat(":").concat(timelineNames.getString(TIMELINENAME)), new Integer(timelineNames.getInt(TIMELINEID)));
    }
    return timelines;
  }
}
