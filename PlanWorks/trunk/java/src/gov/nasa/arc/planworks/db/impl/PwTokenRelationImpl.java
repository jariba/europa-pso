// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenRelationImpl.java,v 1.2 2003-05-18 00:02:26 taylor Exp $
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

  private String key;
  private String masterTokenId;
  private String slaveTokenId;
  
  private PwPartialPlanImpl partialPlan;
  private String collectionName;


  public PwTokenRelationImpl( String key, String masterTokenId, String slaveTokenId,
                              PwPartialPlanImpl partialPlan, String collectionName) {
    this.key = key;
    this.masterTokenId = masterTokenId;
    this.slaveTokenId = slaveTokenId;
    this.collectionName = collectionName;
    this.partialPlan = partialPlan;
  } // end constructor

  /**
   * <code>getKey</code>
   *
   * @return name - <code>String</code> -
   */
  public String getKey() {
    return key;
  }
	
  /**
   * <code>getMasterToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getMasterToken() {
    return partialPlan.getToken( masterTokenId, collectionName);
  }
 
  /**
   * <code>getSlaveToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getSlaveToken() {
    return partialPlan.getToken( slaveTokenId, collectionName);
  }
 

} // end class PwTokenRelationImpl
