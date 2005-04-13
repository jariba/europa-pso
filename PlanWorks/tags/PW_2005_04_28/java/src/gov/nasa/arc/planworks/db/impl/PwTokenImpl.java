// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.55 2004-10-01 20:04:30 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;

/**
 * <code>PwTokenImpl</code> - Java mapping of database structure
 *                       /PartialPlan/Object/Timeline/Slot/Token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenImpl implements PwToken {

  protected Integer id;
  protected boolean isValueToken; // true, unless a Europa constraint token
  //private Integer predicateId;
  protected String predicateName;
  protected Integer startVarId;
  protected Integer endVarId;
  protected Integer durationVarId;
  protected Integer objectVarId;
  protected Integer stateVarId;
  protected Integer parentId;
  protected Integer ruleInstanceId;
  protected List paramVarIds; // element Integer
  private Integer slotId;
  private int slotIndex;
  protected PwPartialPlanImpl partialPlan;


  public PwTokenImpl(final Integer id, final boolean isValueToken, final Integer slotId, 
                     final String predicateName, final Integer startVarId, final Integer endVarId, 
                     final Integer durationVarId, final Integer stateVarId, 
                     final Integer objectVarId, final Integer parentId, 
                     final Integer ruleInstanceId, final String paramVarIds, 
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
    this.paramVarIds = new UniqueSet();
    this.partialPlan = partialPlan;
    this.parentId = parentId;
    this.ruleInstanceId = ruleInstanceId;
    this.slotIndex = -1;
    if(paramVarIds != null) {
      StringTokenizer strTok = new StringTokenizer(paramVarIds, ":");
      while(strTok.hasMoreTokens()) {
        this.paramVarIds.add(Integer.valueOf(strTok.nextToken()));
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
   * <code>getParentId</code>
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
   * <code>getRuleInstanceId</code>
   *
   * @return <code>Integer</code>
   */
  public Integer getRuleInstanceId() {
    return ruleInstanceId;
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
    retval.addAll(getParamVarsList());
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
    return partialPlan.getVariableList(paramVarIds);
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
    return ((parentId == null) || parentId.equals( DbConstants.NO_ID));
  }

  /**
   * <code>isSlotted</code> - in a slot in a timeline
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isSlotted() {
    return ((! isFree()) && (this.slotId != null) && (! slotId.equals( DbConstants.NO_ID))); 
  }

  /**
   * <code>isBaseToken</code> - is slotted and the base token
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isBaseToken() {
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

//These next two methods are in PwTokenImpl because ruleId was once part of the class
//now it can be gotten from the PwRuleInstance class
  /**
   * <code>getModelRule</code>
   *
   * @return - <code>String</code> - 
   */
  public String getModelRule() {
    PwRuleInstance ruleInstance = partialPlan.getRuleInstance(ruleInstanceId);
    PwRule rule = partialPlan.getRule(ruleInstance.getRuleId());
    if(rule == null) {
      return "No rule text";
    }
    return rule.getText();
  }

  /**
   * <code>getRuleId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getRuleId() {
    PwRuleInstance ruleInstance = partialPlan.getRuleInstance(ruleInstanceId);
    return ruleInstance.getRuleId();
  }


  /**
   * <code>addParamVar</code>
   *
   * @param paramVarId - <code>Integer</code> - 
   */
  public void addParamVar(final Integer paramVarId) {
    paramVarIds.add(paramVarId);
  }
  

  public boolean equals(Object o) {
    return ((PwToken)o).getId().equals(id);
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(DbConstants.T_INTERVAL).append("\t");
    if(slotId != null) {
      retval.append(slotId).append("\t").append(slotIndex).append("\t");
    }
    else {
      retval.append("\\N").append("\t").append("\\N").append("\t");
    }
    retval.append( partialPlan.getId()).append("\t");
    if(isFree()) {
      retval.append("1").append("\t");
    }
    else {
      retval.append("0").append("\t");
    }
    if(isValueToken) {
      retval.append("1").append("\t");
    }
    else {
      retval.append("0").append("\t");
    }
    retval.append(startVarId).append("\t");
    retval.append(endVarId).append("\t");
    retval.append(durationVarId).append("\t");
    retval.append(stateVarId).append("\t");
    retval.append(predicateName).append("\t");
    if(isFree()) {
      retval.append("\\N").append("\t").append("\\N").append("\t");
    } else {
      retval.append(parentId).append("\t");
      retval.append(partialPlan.getObject(parentId).getName()).append("\t");
    }
    retval.append(objectVarId).append("\t");
    if(!paramVarIds.isEmpty()) {
      for(ListIterator it = paramVarIds.listIterator(); it.hasNext();) {
        retval.append(it.next()).append(":");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    
    if(isSlotted()) {
      retval.append(partialPlan.getSlot(slotId).getTokenList().indexOf(this)); 
    }
    else {
      // retval.append("-1");
      retval.append("0");
    }
    retval.append("\n");
    return retval.toString();
  }

  public List getNeighbors() {
    LinkedList classes = new LinkedList();
    classes.add(PwRuleInstance.class);
    classes.add(PwVariable.class);
    classes.add(PwObject.class);
    classes.add(PwSlot.class);
    classes.add(PwResource.class);
    return getNeighbors(classes);
  }

  public List getNeighbors(List classes) {
    PwSlot slotParent = null; PwObject objectParent = null;
    PwResource resourceParent = null;
    List retval = new UniqueSet();
    for(Iterator classIt = classes.iterator(); classIt.hasNext();) {
      Class cclass = (Class) classIt.next();
      if(PwRuleInstance.class.isAssignableFrom( cclass)) {
        if ((getRuleInstanceId() != null) && (getRuleInstanceId().intValue() > 0)) {
          retval.add(partialPlan.getRuleInstance(getRuleInstanceId()));
        }
        Iterator slaveIdItr = partialPlan.getSlaveTokenIds( getId()).iterator();
        while (slaveIdItr.hasNext()) {
          Integer ruleInstanceId =
            partialPlan.getToken( (Integer) slaveIdItr.next()).getRuleInstanceId();
          if ((ruleInstanceId != null) && (ruleInstanceId.intValue() > 0)) {
            retval.add( partialPlan.getRuleInstance( ruleInstanceId));
          }
        }
      } else if(PwVariable.class.isAssignableFrom( cclass)) {
        retval.addAll(getVariablesList());
      } else if(cclass.equals(PwSlot.class)) {
        if ((slotId != null) && (! slotId.equals( DbConstants.NO_ID))) {
          slotParent = partialPlan.getSlot(slotId);
        }
      } else if(cclass.equals(PwResource.class)) {
        if ((getParentId() != null) && (! getParentId().equals( DbConstants.NO_ID))) {
          resourceParent = partialPlan.getResource(getParentId());
        }
      } else if(cclass.equals(PwObject.class)) {
        if ((getParentId() != null) && (! getParentId().equals( DbConstants.NO_ID))) {
          objectParent = partialPlan.getObject(getParentId());
        }
      }
    }
    if (slotParent != null) {
      retval.add( slotParent);
    } else if (resourceParent != null) {
      retval.add( resourceParent);
    } else if (objectParent != null) {
      retval.add( objectParent);
    } else {
      // free token
    }
    return new ArrayList( retval);
  }

  public List getNeighbors(List classes, List linkTypes) {
    List retval = new LinkedList();
    for(Iterator it = linkTypes.iterator(); it.hasNext();) {
      String linkType = (String) it.next();
      if(linkType.equals(ViewConstants.RULE_INST_TO_TOKEN_LINK_TYPE) &&
         CollectionUtils.findFirst(new AssignableFunctor(PwRuleInstance.class),
                                   classes) != null) {
        if(getRuleInstanceId() != null && getRuleInstanceId().intValue() > 0) {
          retval.add(partialPlan.getRuleInstance(getRuleInstanceId()));
        }
        Set usedRuleInstanceIds = new TreeSet();
        for(Iterator slaveIdIterator = partialPlan.getSlaveTokenIds(getId()).iterator(); slaveIdIterator.hasNext();) {
          Integer ruleInstanceId =
            partialPlan.getToken((Integer) slaveIdIterator.next()).getRuleInstanceId();
          if(ruleInstanceId != null && ruleInstanceId.intValue() > 0 &&
             !usedRuleInstanceIds.contains(ruleInstanceId)) {
            retval.add(partialPlan.getRuleInstance(ruleInstanceId));
            usedRuleInstanceIds.add(ruleInstanceId);
          }
        }
      }
      else if(linkType.equals(ViewConstants.TOKEN_TO_VARIABLE_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwVariable.class),
                                        classes) != null) {
        retval.addAll(getVariablesList());
      }
      else if(linkType.equals(ViewConstants.SLOT_TO_TOKEN_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwSlot.class), classes) != null &&
              slotId.intValue() > 0) {
        retval.add(partialPlan.getSlot(slotId));
      }
//       else if(linkType.equals(ViewConstants.OBJECT_TO_TOKEN_LINK_TYPE) && 
//               Collections.binarySearch( classes, PwObject.class) >= 0) {
//         retval.add(partialPlan.getObject(getParentId()));
//       }
      else if(linkType.equals(ViewConstants.OBJECT_TO_TOKEN_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwObject.class), classes) != null) {
        if (partialPlan.getObject(getParentId()) != null) {
          retval.add(partialPlan.getObject(getParentId()));
        }
      }
    }
    return retval;
  }

  public List getNeighbors(List classes, Set ids) {
    return PwEntityImpl.getNeighbors(this, classes, ids);
  }

} // end class PwTokenImpl

