//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.17 2003-07-14 20:52:02 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

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
import gov.nasa.arc.planworks.db.util.MySQLDB;
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
  private ArrayList validTokenIds;
  private Long partialPlanKey;
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

  public ContentSpec(PwPartialPlan partialPlan, RedrawNotifier redrawNotifier) 
    throws SQLException {
    this.partialPlanKey = partialPlan.getKey();
    this.redrawNotifier = redrawNotifier;
    this.validTokenIds = new ArrayList();
    ResultSet validTokens = MySQLDB.queryDatabase("SELECT TokenId FROM Token WHERE PartialPlanId=".concat(partialPlanKey.toString()));
    while(validTokens.next()) {
      validTokenIds.add(new Integer(validTokens.getInt("TokenId")));
    }
  }
  /**
   * Sets all of the bits to true, then informs the views governed by the spec that they need to
   * redraw.
   */
  public void resetSpec() throws SQLException {
    validTokenIds.clear();
    ResultSet validTokens = MySQLDB.queryDatabase("SELECT TokenId FROM Token WHERE PartialPlanId=".concat(partialPlanKey.toString()));
    while(validTokens.next()) {
      validTokenIds.add(new Integer(validTokens.getInt("TokenId")));
    }
    redrawNotifier.notifyRedraw();
  }
  public List getValidTokenIds(){return validTokenIds;}
  /**
   * Given a key, returns true or false depending on whether or not the key is in the current
   * spec.
   * @param key the key being tested.
   */
  public boolean isInContentSpec(Integer key) {
    return true;
  }
  public void printSpec() {
    System.err.println("Allowable tokens: ");
    ListIterator tokenIdIterator = validTokenIds.listIterator();
    while(tokenIdIterator.hasNext()) {
      System.err.println(((Integer)tokenIdIterator.next()).toString());
    }
  }
  /**
   * Given a key, returns the integer part.  Used to index into the BitSet.
   * @param key the key being converted.
   */
  private int keyToIndex(Integer key) throws NumberFormatException {
    return 0;
  }
  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid keys through a series of database queries, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   * @param timeline the result of getValues() in TimelineGroupBox.
   * @param predicate the result of getValues() in PredicateGroupBox.
   * @param timeInterval the result of getValues() in TimeIntervalGroupBox.
   */
  public void applySpec(List timeline, List predicate, List timeInterval) 
    throws NumberFormatException, SQLException {
    StringBuffer tokenQuery = new StringBuffer("SELECT TokenId FROM Token WHERE PartialPlanId=");
    tokenQuery.append(partialPlanKey.toString()).append(" ");
    if(timeline != null) {
      tokenQuery.append("&& (TimelineId");
      if(((String)timeline.get(0)).indexOf("not") != -1) {
        tokenQuery.append("!");
      }
      tokenQuery.append("=");
      tokenQuery.append(((Integer)timeline.get(1)).toString()).append(" ");
      for(int i = 2; i < timeline.size(); i++) {
        String connective = (String) timeline.get(i);
        if(connective.indexOf("and") != -1) {
          tokenQuery.append("&& TimelineId");
        }
        else {
          tokenQuery.append("|| TimelineId");
        }
        if(connective.indexOf("not") != -1) {
          tokenQuery.append("!");
        }
        i++;
        tokenQuery.append("=").append(((Integer)timeline.get(i)).toString()).append(" ");
      }
      tokenQuery.append(") ");
    }
    if(predicate != null) {
      tokenQuery.append("&& (PredicateId");
      if(((String)predicate.get(0)).indexOf("not") != -1) {
        tokenQuery.append("!");
      }
      tokenQuery.append("=");
      tokenQuery.append(((Integer)predicate.get(1)).toString()).append(" ");
      for(int i = 2; i < predicate.size(); i++) {
        String connective = (String) predicate.get(i);
        if(connective.indexOf("and") != -1) {
          tokenQuery.append("&& PredicateId");
        }
        else {
          tokenQuery.append("|| PredicateId");
        }
        if(connective.indexOf("not") != -1) {
          tokenQuery.append("!");
        }
        i++;
        tokenQuery.append("=").append(((Integer)predicate.get(i)).toString()).append(" ");
      }
      tokenQuery.append(") ");
    }
    tokenQuery.append(";");
    ResultSet tokenIds = MySQLDB.queryDatabase(tokenQuery.toString());
    validTokenIds.clear();
    while(tokenIds.next()) {
      validTokenIds.add(new Integer(tokenIds.getInt("TokenId")));
    }
    printSpec();
    redrawNotifier.notifyRedraw();
  }
  public Map getPredicateNames() throws SQLException {
    HashMap predicates = new HashMap();
    
    System.err.println("Getting predicate names...");
    ResultSet predicateNames = 
      MySQLDB.queryDatabase("SELECT PredicateName, PredicateId FROM Predicate WHERE PartialPlanId=".concat(partialPlanKey.toString()));
    while(predicateNames.next()) {
      predicates.put(predicateNames.getString("PredicateName"), 
                       new Integer(predicateNames.getInt("PredicateId")));
    }
    return predicates;
  }
  public Map getTimelineNames() throws SQLException {
    HashMap timelines = new HashMap();
    System.err.println("Getting timeline names...");
    ResultSet timelineNames =
      MySQLDB.queryDatabase("SELECT Object.ObjectName, Timeline.TimelineName, Timeline.TimelineId FROM Object RIGHT JOIN Timeline ON Timeline.ObjectId=Object.ObjectId && Timeline.PartialPlanId=Object.PartialPlanId WHERE Object.PartialPlanId=".concat(partialPlanKey.toString()));
    String objName = null;
    while(timelineNames.next()) {
      objName = (timelineNames.getString("Object.ObjectName") == null ? objName : 
                 timelineNames.getString("Object.ObjectName"));
      timelines.put("".concat(objName).concat(":").concat(timelineNames.getString("Timeline.TimelineName")), new Integer(timelineNames.getInt("Timeline.TimelineId")));
    }
    return timelines;
  }
  public void executeQuery(String query) throws SQLException {
    System.err.println("Test: executing query " + query);
    long t1 = System.currentTimeMillis();
    ResultSet queryResult = MySQLDB.queryDatabase(query);
    System.err.println("Test: query took " + (System.currentTimeMillis() - t1) + "ms");
    System.err.println("Test: got " + queryResult.getFetchSize() + " rows.");
    while(queryResult.next()) {
      ResultSetMetaData metaData = queryResult.getMetaData();
      System.err.println("---BEGIN ROW---");
      for(int i = 0; i < metaData.getColumnCount(); i++) {
        StringBuffer output = new StringBuffer(metaData.getColumnLabel(i));
        output.append(" : ");
        switch(metaData.getColumnType(i)) {
        case Types.BIGINT:
          output.append(Long.toString(queryResult.getInt(i)));
          break;
        case Types.BLOB:
          output.append(new String(queryResult.getBlob(i).getBytes(0, (int)queryResult.getBlob(i).length())));
          break;
          /*case Types.BOOLEAN:
          output.append(Boolean.toString(queryResult.getBoolean(i)));
          break;*/
        case Types.INTEGER:
          output.append(Integer.toString(queryResult.getInt(i)));
          break;
        case Types.VARCHAR:
          output.append(queryResult.getString(i));
          break;
        default:
          output.append((new Boolean(queryResult.getBoolean(i))).toString());
          break;
        }
        System.err.println(output.toString());
      }
    }
  }
}
