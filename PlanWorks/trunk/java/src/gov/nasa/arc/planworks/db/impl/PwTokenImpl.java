// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.34 2004-03-03 02:14:20 taylor Exp $
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
  //private Integer predicateId;
  private String predicateName;
  private Integer startVarId;
  private Integer endVarId;
  private Integer durationVarId;
  private Integer objectVarId;
  private Integer stateVarId;
  private Integer parentId;
  private List tokenRelationIds; // element Integer
  private List paramVarIds; // element Integer
  private Integer slotId;
  private int slotIndex;
  private PwPartialPlanImpl partialPlan;


  public PwTokenImpl(final Integer id, final boolean isValueToken, final Integer slotId, 
                     final String predicateName, final Integer startVarId, final Integer endVarId, 
                     final Integer durationVarId, final Integer stateVarId, 
                     final Integer objectVarId, final Integer parentId, 
                     final String tokenRelationIds, final String paramVarIds, 
                     final String tokenInfo, final PwPartialPlanImpl partialPlan)
  {
    this.id = id;
    this.isValueToken = isValueToken;
    this.slotId = slotId;
    //this.predicateId = predicateId;
    this.predicateName = predicateName;
    this.startVarId = startVarId;
    this.endVarId = endVarId;
    this.durationVarId = durationVarId;
    this.objectVarId = objectVarId;
    this.stateVarId = stateVarId;
    this.tokenRelationIds = new UniqueSet();
    this.paramVarIds = new UniqueSet();
    this.partialPlan = partialPlan;
    this.parentId = parentId;
    this.slotIndex = -1;
    if(paramVarIds != null) {
      StringTokenizer strTok = new StringTokenizer(paramVarIds, ":");
      while(strTok.hasMoreTokens()) {
        this.paramVarIds.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    if(tokenRelationIds != null) {
      StringTokenizer strTok = new StringTokenizer(tokenRelationIds, ":");
      while(strTok.hasMoreTokens()) {
        this.tokenRelationIds.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    if(tokenInfo != null) {
      slotIndex = Integer.parseInt(tokenInfo);
    }
    PwObjectImpl parent = (PwObjectImpl) partialPlan.getObject(parentId);
    if(parent instanceof PwTimelineImpl) {
      PwTimelineImpl timeline = (PwTimelineImpl) parent;
      PwSlotImpl slot = timeline.addSlot(slotId);
      slot.addToken(this);
    }
    partialPlan.addToken(id, this);
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
  public Integer getParentId() {
    return parentId;
  }
  
  /**
   * <code>getObjectId</code>
   *
   * @return <code>Integer</code>
   */
  public Integer getObjectId() {
    //return objectId;
    return null; 
  }
  /**
   * <code>getPredicate</code>
   *
   * @return - <code>PwPredicate</code> - 
   */
//   public PwPredicate getPredicate() {
//     return partialPlan.getPredicate( predicateId);
//   }

  public String getPredicateName() {
    return predicateName;
  }

  public String getName() {
    return predicateName;
  }

  protected int getSlotIndex() {return slotIndex;}

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
  public PwVariable getStateVariable() {
    return partialPlan.getVariable( stateVarId);
  }

  /**
   * <code>getTokenRelationIdsList</code>
   *
   * @return - <code>List</code> - of Integer.  Id for token relation establishing this token as
   * a slave
   */
  public List getTokenRelationIdsList() {
    return new ArrayList(tokenRelationIds);
  }

  public List getVariables() {
    return getVariablesList();
  }

  /**
   * <code>getVariablesList</code> - return TokenVars & ParamVars
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getVariablesList() {
    List retval = getTokenVarsList();
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
    retval.add( getStateVariable());
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

  /**
   * <code>isFree</code> - not attached to an object
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isFree() {
    return ((parentId == null) || parentId.equals( DbConstants.noId));
  }

  /**
   * <code>isSlotted</code> - in a slot in a timeline
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isSlotted() {
    return ((! isFree()) && (this.slotId != null) && (! slotId.equals( DbConstants.noId)));
  }

  /**
   * <code>isBaseToken</code> - is slotted and the base token
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isBaseToken() {
//     System.err.println ( "isSlotted " + isSlotted() + " getSlotId " + getSlotId());
//     System.err.println ( " slot " + partialPlan.getSlot( getSlotId()));
//     System.err.println ( " baseToken " + partialPlan.getSlot( getSlotId()).getBaseToken());
//     System.err.println ( " baseTokenId " +
//                          partialPlan.getSlot( getSlotId()).getBaseToken().getId());
    return (isSlotted() &&
            (partialPlan.getSlot( getSlotId()).getBaseToken().getId().equals( id)));
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer( predicateName);
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
    // this change adds the suffix of partial plan id to all token node tool tip displays
    // if this suffix is needed, then another method needs to handle it - will 24oct03
    //buffer.append( " )").append("  ").append(partialPlan.getId());
    buffer.append( " )");
    return buffer.toString();
  }

  /**
   * <code>addParamVar</code>
   *
   * @param paramVarId - <code>Integer</code> - 
   */
  public void addParamVar(final Integer paramVarId) {
    paramVarIds.add(paramVarId);
  }
  
  /**
   * <code>addTokenRelation</code>
   *
   * @param tokenRelationId - <code>Integer</code> - 
   */
  public void addTokenRelation(final Integer tokenRelationId) {
    tokenRelationIds.add(tokenRelationId);
  }

} // end class PwTokenImpl

