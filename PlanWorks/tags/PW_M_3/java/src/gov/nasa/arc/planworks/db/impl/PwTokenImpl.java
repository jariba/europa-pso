// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.15 2003-07-15 00:21:51 miatauro Exp $
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

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwVariable;
//import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.MySQLDB;

/**
 * <code>PwTokenImpl</code> - Java mapping of XML structure
 *                       /PartialPlan/Object/Timeline/Slot/Token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenImpl implements PwToken {

  private Integer key;
  private boolean isValueToken;
  private Integer predicateId;
  private Integer startVarId;
  private Integer endVarId;
  private Integer durationVarId;
  private Integer objectVarId;
  private Integer objectId;
  private Integer rejectVarId;
  private Integer timelineId;
  private List tokenRelationIds; // element String
  private List paramVarIds; // element String
  private Integer slotId;
  
  private PwPartialPlanImpl partialPlan;


  public PwTokenImpl(Integer key, boolean isValueToken, Integer slotId, Integer predicateId, 
                     Integer startVarId, Integer endVarId, Integer durationVarId, 
                     Integer objectId, Integer rejectVarId, Integer objectVarId,
                     Integer timelineId, List tokenRelationIds, List paramVarIds, 
                     PwPartialPlanImpl partialPlan)
  {
    this.key = key;
    this.isValueToken = isValueToken;
    this.slotId = slotId;
    this.predicateId = predicateId;
    this.startVarId = startVarId;
    this.endVarId = endVarId;
    this.durationVarId = durationVarId;
    this.objectVarId = objectVarId;
    this.objectId = objectId;
    this.rejectVarId = rejectVarId;
    this.tokenRelationIds = tokenRelationIds;
    this.paramVarIds = paramVarIds;
    this.partialPlan = partialPlan;
    this.timelineId = timelineId;
  }
		
  /**
   * <code>getKey</code>
   *
   * @return name - <code>Integer</code> -
   */
  public Integer getKey() {
    return key;
  }

  public Integer getTimelineId() {
    return timelineId;
  }
  
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
   * <code>getTokenRelationsList</code>
   *
   * @return - <code>List</code> - of PwTokenRelation
   */
  public List getTokenRelationsList() {
    List retval = new ArrayList( tokenRelationIds.size());
    for (int i = 0; i < tokenRelationIds.size(); i++) {
      retval.add( partialPlan.getTokenRelation( (Integer) tokenRelationIds.get( i)));
    }
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
   * @return - <code>String</code> - 
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



} // end class PwTokenImpl

