// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenRelationImpl.java,v 1.5 2003-06-26 18:20:07 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 16May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenRelation;


/**
 * <code>PwTokenRelationImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenRelationImpl implements PwTokenRelation {

  private Integer key;
  private Integer tokenAId;
  private Integer tokenBId;
  private String type;
  
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwTokenRelationImpl</code> - constructor 
   *
   * @param key - <code>Integer</code> - 
   * @param tokenAId - <code>Integer</code> - 
   * @param tokenBId - <code>Integer</code> - 
   * @param type - <code>String</code> -
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwTokenRelationImpl( Integer key, Integer tokenAId, Integer tokenBId, String type,
                              PwPartialPlanImpl partialPlan) {
    this.key = key;
    this.tokenAId = tokenAId;
    this.tokenBId = tokenBId;
    this.type = type;
    this.partialPlan = partialPlan;
  } // end constructor

  /**
   * <code>getKey</code>
   *
   * @return name - <code>Integer</code> -
   */
  public Integer getKey() {
    return key;
  }
	
  /**
   * <code>getTokenAId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getTokenAId() {
    return tokenAId;
  }
 
  /**
   * <code>getTokenBId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getTokenBId() {
    return tokenBId;
  }

  /**
   * <code>getTokenA</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getTokenA() {
    return partialPlan.getToken( tokenAId);
  }
 
  /**
   * <code>getTokenB</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getTokenB() {
    return partialPlan.getToken( tokenBId);
  }
 
  public String getType() {
    return type;
  }
} // end class PwTokenRelationImpl
