// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRuleInstanceImpl.java,v 1.4 2004-07-29 20:31:44 taylor Exp $
//
// PlanWorks -- 
//
// Patrick Daley -- started 17May04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.util.UniqueSet;


/**
 * <code>PwRuleInstanceImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwRuleInstanceImpl implements PwRuleInstance {

  protected Integer id;
  protected Integer ruleId;
  protected Integer masterId;
  protected String  ruleName;
  protected List slaveIds;
  protected List ruleVarIds;
  
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwRuleInstanceImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param ruleId - <code>Integer</code> - 
   * @param masterId - <code>Integer</code> - 
   * @param slaveIds - <code>String</code> -
   * @param ruleVarIds - <code>String</code> -
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwRuleInstanceImpl( final Integer id, final Integer ruleId, final Integer masterId, 
                             final String slaveIds, final String ruleVarIds,
                             final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.ruleId = ruleId;
    this.masterId = masterId;
    this.ruleName = "";
    this.slaveIds = new UniqueSet();
    this.ruleVarIds = new UniqueSet();
    this.partialPlan = partialPlan;
    if(slaveIds != null) {
      StringTokenizer strTok = new StringTokenizer(slaveIds, ",");
      while(strTok.hasMoreTokens()) {
        this.slaveIds.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    if(ruleVarIds != null) {
      StringTokenizer strTok = new StringTokenizer(ruleVarIds, ",");
      while(strTok.hasMoreTokens()) {
        this.ruleVarIds.add(Integer.valueOf(strTok.nextToken()));
      }
    }
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return name - <code>Integer</code> -
   */
  public Integer getId() {
    return id;
  }
	
  /**
   * <code>getRuleId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getRuleId() {
    return ruleId;
  }
 
  /**
   * <code>getMasterId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getMasterId() {
    return masterId;
  }

  /**
   * <code>getSlaveIdsList</code>
   *
   * @return - <code>List</code> - of Integer. 
   */
  public List getSlaveIdsList() {
    return new ArrayList(slaveIds);
  }

  /**
   * <code>getRuleVarIdList</code>
   *
   * @return - <code>List</code> - of Integer. 
   */
  public List getRuleVarIdList() {
    return new ArrayList(ruleVarIds);
  }

  /**
   * <code>getVariables</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getVariables() {
    return partialPlan.getVariableList(ruleVarIds);
  }

  public String getName() {
    return ruleName;
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(partialPlan.getId().toString()).append("\t").append(partialPlan.getSequence().getId());
    retval.append("\t").append(ruleId.toString()).append("\t").append(masterId.toString()).append("\t");
    if(!slaveIds.isEmpty()) {
      for(ListIterator it = slaveIds.listIterator(); it.hasNext();) {
        retval.append(it.next().toString()).append(",");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    if(!ruleVarIds.isEmpty()) {
      for(ListIterator it = ruleVarIds.listIterator(); it.hasNext();) {
        retval.append(it.next().toString()).append(",");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\n");

    return retval.toString();
  } // end toOutputString

  public String toOutputStringSlaveMap() {
    StringBuffer retval = new StringBuffer( "");
    for(ListIterator it = slaveIds.listIterator(); it.hasNext();) {
      retval.append( id.toString()).append("\t");
      retval.append( it.next().toString()).append("\t");
      retval.append( partialPlan.getId().toString()).append("\n");
    }
    return retval.toString();
  } // end toOutputStringSlaveMap

} // end class PwRuleInstanceImpl
