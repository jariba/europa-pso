//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.12 2003-06-30 21:06:53 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
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

  public ContentSpec(PwPartialPlan partialPlan, RedrawNotifier redrawNotifier) {
    this.partialPlanKey = partialPlan.getKey();
    this.redrawNotifier = redrawNotifier;
    
    currentSpec = new BitSet(partialPlan.getMaxKey() - partialPlan.getMinKey() + 1);
    currentSpec.set(0, partialPlan.getMaxKey()-partialPlan.getMinKey(), true);
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
        case Types.BOOLEAN:
          output.append(Boolean.toString(queryResult.getBoolean(i)));
          break;
        case Types.INTEGER:
          output.append(Integer.toString(queryResult.getInt(i)));
          break;
        case Types.VARCHAR:
          output.append(queryResult.getString(i));
        }
        System.err.println(output.toString());
      }
    }
  }
}
