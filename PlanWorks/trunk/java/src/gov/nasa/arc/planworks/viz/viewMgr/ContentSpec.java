//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpec.java,v 1.15 2003-07-08 20:44:18 miatauro Exp $
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
  
  private BitSet currentSpec;
  private Long partialPlanKey;
  private RedrawNotifier redrawNotifier;
  private int offset;
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
    offset = partialPlan.getMinKey();
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
  public boolean isInContentSpec(Integer key) {
    int index = keyToIndex(key);
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
  private int keyToIndex(Integer key) throws NumberFormatException {
    return key.intValue() - offset;
  }
  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid keys through a series of database queries, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   * @param timeline the result of getValues() in TimelineGroupBox.
   * @param predicate the result of getValues() in PredicateGroupBox.
   * @param timeInterval the result of getValues() in TimeIntervalGroupBox.
   */
  //REALLY INEFFICIENT INITIAL RUN.  some of this should probably be refactored
  public void applySpec(List timeline, List predicate, List timeInterval) 
    throws NumberFormatException {
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
