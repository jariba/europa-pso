// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.23 2003-10-02 23:24:21 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.util.UniqueSet;

/**
 * <code>PwTokenImpl</code> - Java mapping of database structure
 *                       /PartialPlan/Object/Timeline/Slot/Token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenImpl implements PwToken {

  private Integer id;
  private boolean isValueToken;
  private Integer predicateId;
  private Integer startVarId;
  private Integer endVarId;
  private Integer durationVarId;
  private Integer objectVarId;
  private Integer objectId;
  private Integer rejectVarId;
  private Integer timelineId;
  private List tokenRelationIds; // element Integer
  private List paramVarIds; // element Integer
  private Integer slotId;
  
  private PwPartialPlanImpl partialPlan;


  public PwTokenImpl(Integer id, boolean isValueToken, Integer slotId, Integer predicateId, 
                     Integer startVarId, Integer endVarId, Integer durationVarId, 
                     Integer objectId, Integer rejectVarId, Integer objectVarId,
                     Integer timelineId, List tokenRelationIds, List paramVarIds, 
                     PwPartialPlanImpl partialPlan)
  {
    this.id = id;
    this.isValueToken = isValueToken;
    this.slotId = slotId;
    this.predicateId = predicateId;
    this.startVarId = startVarId;
    this.endVarId = endVarId;
    this.durationVarId = durationVarId;
    this.objectVarId = objectVarId;
    this.objectId = objectId;
    this.rejectVarId = rejectVarId;
    this.tokenRelationIds = new UniqueSet(tokenRelationIds);
    this.paramVarIds = new UniqueSet(paramVarIds);
    this.partialPlan = partialPlan;
    this.timelineId = timelineId;
  }

  public PwTokenImpl(Integer id, boolean isValueToken, Integer slotId, Integer predicateId,
                     Integer startVarId, Integer endVarId, Integer durationVarId,
                     Integer objectId, Integer rejectVarId, Integer objectVarId,
                     Integer timelineId, PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.isValueToken = isValueToken;
    this.slotId = slotId;
    this.predicateId = predicateId;
    this.startVarId = startVarId;
    this.endVarId = endVarId;
    this.durationVarId = durationVarId;
    this.objectVarId = objectVarId;
    this.objectId = objectId;
    this.rejectVarId = rejectVarId;
    this.tokenRelationIds = new UniqueSet();
    this.paramVarIds = new UniqueSet();
    this.partialPlan = partialPlan;
    this.timelineId = timelineId;
  }
  
  /**
   * <code>getId</code>
   *
   * @return <code>Integer</code>
   */
  public Integer getId() {
    return id;
  }

  /**
   * <code>getTimelineId</code>
   *
   * @return <code>Integer</code>
   */
  public Integer getTimelineId() {
    return timelineId;
  }
  
  /**
   * <code>getObjectId</code>
   *
   * @return <code>Integer</code>
   */
  public Integer getObjectId() {
    return objectId;
  }
  /**
   * <code>getPredicate</code>
   *
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate() {
    return partialPlan.getPredicate( predicateId);
  }

  /**
   * <code>getStartVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getStartVariable() {
    return partialPlan.getVariable( startVarId);
  }
		
  /**
   * <code>getEndVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getEndVariable() {
    return partialPlan.getVariable( endVarId);
  }

  /**
   * <code>getEarliestStart</code> - get lowest value of start variable's domain
   *
   * @return <code>Integer</code> - earliest timepoint
   */

  public Integer getEarliestStart() {
    String earliestStart = partialPlan.getVariable(startVarId).getDomain().getLowerBound();
    if(earliestStart.equals(DbConstants.PLUS_INFINITY)) {
      return new Integer(DbConstants.PLUS_INFINITY_INT);
    }
    else if(earliestStart.equals(DbConstants.MINUS_INFINITY)) {
      return new Integer(DbConstants.MINUS_INFINITY_INT);
    }
    return new Integer(earliestStart);
  }

  /**
   * <code>getLatestEnd</code> - get highest value of end variable's domain
   *
   * @return <code>Integer</code> - latest timepoint
   */

  public Integer getLatestEnd() {
    String latestEnd = partialPlan.getVariable(endVarId).getDomain().getUpperBound();
    if(latestEnd.equals(DbConstants.PLUS_INFINITY)) {
      return new Integer(DbConstants.PLUS_INFINITY_INT);
    }
    else if(latestEnd.equals(DbConstants.MINUS_INFINITY)) {
      return new Integer(DbConstants.MINUS_INFINITY_INT);
    }
    return new Integer(latestEnd);
}

  /**
   * <code>getDurationVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getDurationVariable() {
    return partialPlan.getVariable( durationVarId);
  }

  /**
   * <code>getObjectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getObjectVariable() {
    return partialPlan.getVariable( objectVarId);
  }

  /**
   * <code>getRejectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getRejectVariable() {
    return partialPlan.getVariable( rejectVarId);
  }

  /**
   * <code>getTokenRelationIdsList</code>
   *
   * @return - <code>List</code> - of Integer
   */
  public List getTokenRelationIdsList() {
    return new ArrayList(tokenRelationIds);
  }

  /**
   * <code>getVariablesList</code> - return TokenVars & ParamVars
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getVariablesList() {
    List retval = new ArrayList( 5 + paramVarIds.size());
    retval.add( getStartVariable());
    retval.add( getEndVariable());
    retval.add( getDurationVariable());
    retval.add( getObjectVariable());
    retval.add( getRejectVariable());
    for (int i = 0; i < paramVarIds.size(); i++) {
      retval.add( partialPlan.getVariable( (Integer) paramVarIds.get( i)));
    }
    return retval;
  }

  /**
   * <code>getTokenVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getTokenVarsList() {
    List retval = new ArrayList( 5);
    retval.add( getStartVariable());
    retval.add( getEndVariable());
    retval.add( getDurationVariable());
    retval.add( getObjectVariable());
    retval.add( getRejectVariable());
    return retval;
  }

  /**
   * <code>getParamVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getParamVarsList() {
    List retval = new ArrayList( paramVarIds.size());
    for (int i = 0; i < paramVarIds.size(); i++) {
      retval.add( partialPlan.getVariable( (Integer) paramVarIds.get( i)));
    }
    return retval;
  }

  /**
   * <code>getSlotId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getSlotId() {
    return this.slotId;
  }

  public boolean isFreeToken() {
    return this.slotId == null;
  }
  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer( getPredicate().getName());
    buffer.append( " ( ");
    List paramVarsList = getParamVarsList();
    for (int i = 0; i < paramVarsList.size() - 1; i++) {
      buffer.append( ((PwDomain) ((PwVariable) paramVarsList.get( i)).
                      getDomain()).toString());
      buffer.append( ", ");
    }
    if (paramVarsList.size() > 0) {
      buffer.append( ((PwDomain) ((PwVariable) paramVarsList.
                                  get( paramVarsList.size() - 1)).getDomain()).toString());
    }
    buffer.append( " )");
    return buffer.toString();
  }

  /**
   * <code>addParamVar</code>
   *
   * @param paramVarId - <code>Integer</code> - 
   */
  public void addParamVar(Integer paramVarId) {
    paramVarIds.add(paramVarId);
  }
  
  /**
   * <code>addTokenRelation</code>
   *
   * @param tokenRelationId - <code>Integer</code> - 
   */
  public void addTokenRelation(Integer tokenRelationId) {
    tokenRelationIds.add(tokenRelationId);
  }

} // end class PwTokenImpl

