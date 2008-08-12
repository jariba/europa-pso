//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanContentSpec.java,v 1.22 2006-10-03 16:14:17 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.viewMgr.RedrawNotifier;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;

/*
 * <code>ContentSpec</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The content specification class.  Interfaces directely with the database to determine which ids
 * are/should be in the current specification, and provides a method for exposing that information
 * to other classes (VizView through ViewSet).
 */

public class PartialPlanContentSpec implements ContentSpec {
  private UniqueSet validTokenIds;
  private List currentSpec;
  private Long partialPlanId;
  private PwPartialPlan partialPlan;
  private RedrawNotifier redrawNotifier;

  private static final String AND = "and";
  private static final String AND_PPREDICATENAME = "AND (PredicateName";
  private static final String AND_PREDICATENAME = "AND PredicateName";
  private static final String AND_PTIMELINEID = "AND (ParentId";
  private static final String AND_TIMELINEID = "AND ParentId";
  private static final String EQ = "=";
  private static final String NEG = "!";
  private static final String NOT = "not";
  //private static final String OBJECTNAME = "Object.ObjectName";
  private static final String OBJECTNAME = "ObjectName";
  //private static final String OBJECTID = "Object.ObjectId";
  private static final String OBJECTID = "ObjectId";
  private static final String OR = "or";
  private static final String OR_TIMELINEID = "OR ParentId";
  private static final String OR_PREDICATENAME = "OR PredicateName";
  private static final String PREDICATEID = "PredicateId";
  private static final String PREDICATENAME = "PredicateName";
  private static final String PREDICATENAME_QUERY =
    "SELECT DISTINCT PredicateName FROM Token WHERE PartialPlanId=";
  //private static final String TIMELINEID = "Token.ParentId";
  //private static final String TIMELINENAME = "Token.TimelineName";
  private static final String TIMELINENAME_QUERY =
    //"SELECT DISTINCT Object.ObjectName, Object.ObjectId FROM Object WHERE Object.ObjectType=" 
    "SELECT ObjectName, ObjectId FROM Object WHERE ObjectType=" 
    + DbConstants.O_TIMELINE + " AND PartialPlanId=";
    //+ DbConstants.O_TIMELINE + " AND Object.PartialPlanId=";
  private static final String TOKENID = "TokenId";
  private static final String TOKENID_QUERY = "SELECT TokenId FROM Token WHERE PartialPlanId=";
  public static final int FREE_ONLY = -1;
  public static final int SLOTTED_ONLY = 0;
  public static final int ALL = 1;
  public static final String REQUIRE = "req";
  public static final String EXCLUDE = "ex";

  /**
   * Creates the ContentSpec object, then makes a query for all valid tokens
   *
   * @param <code>partialPlan</code> the partial plan object constrained by this object
   * @param <code>redrawNotifier</code> an interface to inform views that they need to re-draw
   */

  public PartialPlanContentSpec(final ViewableObject partialPlan, 
                                final RedrawNotifier redrawNotifier)  {
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.partialPlanId = this.partialPlan.getId();
    this.redrawNotifier = redrawNotifier;
    this.validTokenIds = new UniqueSet();
    queryValidTokens();
    currentSpec = partialPlan.getContentSpec();

    currentSpec.add( null); // timeline
    currentSpec.add( null); // predicate
    currentSpec.add( null); // time interval
    currentSpec.add( new Boolean( false)); // merge
    currentSpec.add( new Integer( ALL)); // tokenTypes
    currentSpec.add(null);

    applySpec(currentSpec);
  }

  /**
   * Sets all tokens valid
   */
  public void resetSpec() {
    validTokenIds.clear();
    currentSpec.clear();
    currentSpec.add( null); // timeline
    currentSpec.add( null); // predicate
    currentSpec.add( null); // time interval
    currentSpec.add( new Boolean( false)); // merge
    currentSpec.add( new Integer( ALL)); // tokenTypes
    currentSpec.add(null);
    queryValidTokens();
    redrawNotifier.notifyRedraw();
  }
 
  public void resetSpecFromPlan() {
    applySpec(partialPlan.getContentSpec());
  }
 
  /**
   * Get all token ids
   */
  private void queryValidTokens() {
    try {
      ResultSet validTokens = SQLDB.queryDatabase(TOKENID_QUERY.concat(partialPlanId.toString()));
      while(validTokens.next()) {
        validTokenIds.add(new Integer(validTokens.getInt(TOKENID)));
      }
    }
    catch(SQLException sqle) {
    }
  }

  /**
   * Get the list of ids for tokens conforming to the specification
   *
   * @return List - valid token ids
   */
  public List getValidIds() {
    return validTokenIds;
  }

  /**
   * <code>getPartialPlan</code>
   *
   * @return - <code>PwPartialPlan</code> - 
   */
  public PwPartialPlan getPartialPlan() {
    return partialPlan;
  }

  public void printSpec() {
    System.err.println("Allowable tokens: ");
    ListIterator tokenIdIterator = validTokenIds.listIterator();
    while(tokenIdIterator.hasNext()) {
      System.err.println(((Integer)tokenIdIterator.next()).toString());
    }
  }

  public void printContentSpecification() {
    printSpecLists(currentSpec);
  }

  private void printSpecLists(final List list) {
    List timeline = (List) list.get(0);
    List predicate = (List) list.get(1);
    List timeInterval = (List) list.get(2);
    boolean mergeTokens = ((Boolean)list.get(3)).booleanValue();
    int tokenTypes = ((Integer)list.get(4)).intValue();
    List uniqueKeys = (List) list.get(5);
    System.err.println("Timeline: ");
    if(timeline != null) {
      StringBuffer timelineStr = new StringBuffer();
      for(int i = 0; i < timeline.size(); i += 2) {
        timelineStr.append((String)timeline.get(i)).append(" ");
        timelineStr.append(((Integer)timeline.get(i+1)).toString()).append(" ");
      }
      System.err.println(timelineStr.toString());
    }
    System.err.println("Predicate: ");
    if(predicate != null) {
      StringBuffer predicateStr = new StringBuffer();
      for(int i = 0; i < predicate.size(); i += 2) {
        predicateStr.append((String)predicate.get(i)).append(" ");
        predicateStr.append((String)predicate.get(i+1)).append(" ");
      }
      System.err.println(predicateStr.toString());
    }
    System.err.println("Time Interval: ");
    if(timeInterval != null) {
      StringBuffer timeIntervalStr = new StringBuffer();
      for(int i = 0; i < timeInterval.size(); i += 3) {
        timeIntervalStr.append((String)timeInterval.get(i)).append(" ");
        timeIntervalStr.append(((Integer)timeInterval.get(i+1)).toString()).append("-");
        timeIntervalStr.append(((Integer)timeInterval.get(i+2)).toString()).append(" ");
      }
      System.err.println(timeIntervalStr.toString());
    }
    System.err.println("Merge tokens " + mergeTokens);
    System.err.println("Viewing types " + tokenTypes);
    System.err.println("Uniquely Specified: ");
    if(uniqueKeys != null) {
      StringBuffer uniqueStr = new StringBuffer();
      for(int i = 0; i < uniqueKeys.size(); i += 2) {
        uniqueStr.append((String)uniqueKeys.get(i)).append(" ");
        uniqueStr.append(((Integer)uniqueKeys.get(i+1)).toString());
        if(i != uniqueKeys.size() - 2) {
          uniqueStr.append(" ^ ");
        }
      }
      System.err.println(uniqueStr.toString());
    }
  }

  /**
   * Given the parametes specified by the user in the ContentSpecWindow, constructs the entire
   * specification of valid ids through a database query, then informs the windows
   * goverend by this spec that they need to redraw themselves to the new specification.
   * @param timeline the result of getValues() in TimelineGroupBox.
   * @param predicate the result of getValues() in PredicateGroupBox.
   * @param timeInterval the result of getValues() in TimeIntervalGroupBox.
   * @param mergeTokens the result of getValue() in MergeBox.
   * @param tokenTypes the result of getValue() in TokenTypeBox.
   */
  public void applySpec(final List spec) throws NumberFormatException {
    if(spec.size() != 6)
      return;
    if(!specChanged(spec)) {
      return;
    }
    List timeline = (List) spec.get(0);
    List predicate = (List) spec.get(1);
    List timeInterval = (List) spec.get(2);
    boolean mergeTokens = ((Boolean)spec.get(3)).booleanValue();
    int tokenTypes = ((Integer)spec.get(4)).intValue();
    List uniqueKeys = (List) spec.get(5);
    currentSpec = spec;
    //printSpecLists(spec);
    try {
      StringBuffer tokenQuery = new StringBuffer(TOKENID_QUERY);
      tokenQuery.append(partialPlanId.toString()).append(" ");
      if(timeline != null && timeline.size() != 0) {
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
      if(predicate != null && predicate.size() != 0) {
        tokenQuery.append(AND_PPREDICATENAME);
        if(((String)predicate.get(0)).indexOf(NOT) != -1) {
          tokenQuery.append(NEG);
        }
        tokenQuery.append(EQ);
        //tokenQuery.append(((Integer)predicate.get(1)).toString()).append(" ");
        tokenQuery.append("'").append(predicate.get(1)).append("' ");
        for(int i = 2; i < predicate.size(); i++) {
          String connective = (String) predicate.get(i);
          if(connective.indexOf(AND) != -1) {
            tokenQuery.append(AND_PREDICATENAME);
          }
          else {
            tokenQuery.append(OR_PREDICATENAME);
          }
          if(connective.indexOf(NOT) != -1) {
            tokenQuery.append(NEG);
          }
          i++;
          tokenQuery.append(EQ).append("'").append(predicate.get(i)).append("' ");
        }
        tokenQuery.append(") ");
      }
      tokenQuery.append(";");
      //System.err.println( "applySpec " + tokenQuery.toString());
      ResultSet tokenIds = SQLDB.queryDatabase(tokenQuery.toString());
      validTokenIds.clear();
      while(tokenIds.next()) {
        validTokenIds.add(new Integer(tokenIds.getInt(TOKENID)));
      }
      if(tokenTypes == FREE_ONLY) {
        ListIterator freeTokenIdIterator = validTokenIds.listIterator();
        while(freeTokenIdIterator.hasNext()) {
          Integer id = (Integer) freeTokenIdIterator.next();
          if(!partialPlan.getToken(id).isFree()) {
            freeTokenIdIterator.remove();
          }
        }
      }
      else if(tokenTypes == SLOTTED_ONLY) {
        ListIterator slottedTokenIdIterator = validTokenIds.listIterator();
        while(slottedTokenIdIterator.hasNext()) {
          Integer id = (Integer) slottedTokenIdIterator.next();
          if(!partialPlan.getToken(id).isSlotted()) {
            slottedTokenIdIterator.remove();
          }
        }
      }
      else if(tokenTypes != ALL) {
        System.err.println("Invalid tokenType value " + tokenTypes + ".  Ignoring.");
      }
      if(mergeTokens) {
        UniqueSet tempValidIds = new UniqueSet();
        ListIterator mergeTokenIdIterator = validTokenIds.listIterator();
        while(mergeTokenIdIterator.hasNext()) {
          Integer id = (Integer) mergeTokenIdIterator.next();
          if(partialPlan.getSlot(partialPlan.getToken(id).getSlotId()) != null) {
            tempValidIds.add(partialPlan.getSlot(partialPlan.getToken(id).getSlotId()).getBaseToken().getId());
          }
          else if(partialPlan.getToken(id).isFree()) {
            tempValidIds.add(id);
          }
        }
        validTokenIds.clear();
        validTokenIds.addAll(tempValidIds);
      }
      if(timeInterval != null) {
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
            else if(connective.indexOf(OR) > -1) {
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
      if(uniqueKeys != null) {
        for(int i = 0; i < uniqueKeys.size(); i += 2) {
          String op = (String) uniqueKeys.get(i);
          Integer key = (Integer) uniqueKeys.get(i+1);
          if(op.equals(REQUIRE)) {
            validTokenIds.add(key);
          }
          else if(op.equals(EXCLUDE)) {
            validTokenIds.remove(key);
          }
        }
      }
    }
    catch(SQLException sqle) {
    }
    redrawNotifier.notifyRedraw();
  }

  /**
   * Given a time interval and a token's earliest start and latest end times, determines whether or not
   * the token exists in the interval.
   *
   * @param connective The logic function to apply (negation)
   * @param start The beginning of the interval
   * @param end The end of the interval
   * @param earliestStart The earliest start time of the token
   * @param latestEnd The latest end time of the token
   */

  private boolean evaluateTimeInterval(final String connective, final Integer start,
                                       final Integer end, final Integer earliestStart,
                                       final Integer latestEnd) {
    boolean negation = (connective.indexOf(NOT) > -1);
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

  /**
   * Maps predicate names to predicate Ids for use in the ContentSpecWindow
   *
   * @return Map name => Id
   */

  public Map getPredicateNames() {
    Map predicates = new HashMap();
    
    System.err.println("   ... Getting predicate names");
    try {
      ResultSet predicateNames = 
        SQLDB.queryDatabase(PREDICATENAME_QUERY.concat(partialPlanId.toString()));
      while(predicateNames.next()) {
        predicates.put(predicateNames.getString(PREDICATENAME),
                       predicateNames.getString(PREDICATENAME));
      }
    }
    catch(SQLException sqle) {}
    return predicates;
  }

  /**
   * Maps timeline names to timeline Ids for use in the ContentSpecWindow
   *
   * @return Map name => Id
   */

  public Map getTimelineNames() {
    HashMap timelines = new HashMap();
    long t1 = System.currentTimeMillis();
    System.err.println("   ... Getting timeline names");
    try {
      ResultSet timelineNames =
        SQLDB.queryDatabase(TIMELINENAME_QUERY.concat(partialPlanId.toString()));
      //String objName = null;
      while(timelineNames.next()) {
        timelines.put(timelineNames.getString(OBJECTNAME), 
                      new Integer(timelineNames.getInt(OBJECTID)));
        //timelines.put("".concat(objName).concat(":").concat(timelineNames.getString(TIMELINENAME)), new Integer(timelineNames.getInt(TIMELINEID)));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return timelines;
  }
  
  public List getCurrentSpec() {
    return new ArrayList(currentSpec);
  }

  private boolean specChanged(final List newSpec) {
    if(currentSpec.size() != newSpec.size()) {
      return true;
    }
    ListIterator newIterator = newSpec.listIterator();
    ListIterator oldIterator = currentSpec.listIterator();
    while(newIterator.hasNext()) {
      Object newObject = newIterator.next();
      Object oldObject = oldIterator.next();
      if(newObject == null ^ oldObject == null) {
        return true;
      }
      if(newObject == null && oldObject == null) {
        continue;
      }
      if(newObject instanceof List && oldObject instanceof List) {
        List newList = (List) newObject;
        List oldList = (List) oldObject;
        if(newList.size() != oldList.size()) {
          return true;
        }
        ListIterator newSubIterator = newList.listIterator();
        ListIterator oldSubIterator = oldList.listIterator();
        while(newSubIterator.hasNext()) {
          Object newItem = newSubIterator.next();
          Object oldItem = oldSubIterator.next();
          if(!newItem.equals(oldItem)) {
            return true;
          }
        }
      }
      else if((newObject instanceof Boolean && oldObject instanceof Boolean) ||
              (newObject instanceof Integer && oldObject instanceof Integer)) {
        if(!newObject.equals(oldObject)) {
          return true;
        }
      }
    }
    return false;
  }
}
